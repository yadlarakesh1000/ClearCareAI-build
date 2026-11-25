package com.clearcareai.modules.voicereview.serviceimpl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.clearcareai.config.OmniDimProperties;
import com.clearcareai.exception.BadRequestException;
import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.consultation.entity.Consultation;
import com.clearcareai.modules.consultation.repository.ConsultationRepository;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.review.entity.Review;
import com.clearcareai.modules.review.repository.ReviewRepository;
import com.clearcareai.modules.voicereview.config.VoiceReviewProperties;
import com.clearcareai.modules.voicereview.dto.OmniDimCallResponse;
import com.clearcareai.modules.voicereview.dto.VoiceReviewTriggerRequest;
import com.clearcareai.modules.voicereview.service.VoiceReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// WHY THERE IS NO @Transactional ON THE PUBLIC METHODS:
// the OmniDim HTTP call can take seconds. Inside a database transaction it would
// hold a database connection for that whole time. So every call is done in 3 steps:
//   1. short transaction  - count the attempt, read the data the call needs
//   2. NO transaction     - the HTTP call to OmniDim
//   3. short transaction  - store the call id OmniDim returned
// TransactionTemplate runs one block of code inside a transaction. We use it
// instead of @Transactional because @Transactional does not work when a method
// is called from another method of the same class (self-invocation).
@Service
@Slf4j
@RequiredArgsConstructor
public class VoiceReviewServiceImpl implements VoiceReviewService {
    private final ReviewRepository reviewRepository;
    private final ConsultationRepository consultationRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final OmniDimProperties omniDimProperties;
    private final VoiceReviewProperties voiceReviewProperties;
    private final TransactionTemplate transactionTemplate;

    private Doctor getDoctorByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException(email));
        Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseThrow(() -> new ResourceNotFoundException(email));
        return doctor;
    }

    // runs inside a transaction (called through transactionTemplate)
    private Long createPendingVoiceReview(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", consultationId));
        Review review = Review.builder()
                .consultation(consultation)
                .patient(consultation.getPatient())
                .doctor(consultation.getDoctor())
                .status(Review.Status.PENDING)
                .source(Review.Source.VOICE)
                .isFlagged(false)
                .callAttempts(0)
                .build();
        Review saved = reviewRepository.save(review);
        return saved.getId();
    }

    // runs inside a transaction (called through transactionTemplate).
    // Checks the doctor's request, creates the PENDING review, and returns the
    // part of the response we already know (review id and patient phone).
    private OmniDimCallResponse validateAndCreateReview(String email, Long consultationId) {
        Doctor doctor = getDoctorByEmail(email);

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", consultationId));

        if (!consultation.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("You are not authorized to trigger a review for this consultation");
        }

        if (consultation.getStatus() != Consultation.Status.COMPLETED) {
            throw new BadRequestException("Voice review can only be triggered for a completed consultation");
        }

        if (reviewRepository.existsByConsultationId(consultation.getId())) {
            throw new BadRequestException("A review already exists for this consultation");
        }

        Long reviewId = createPendingVoiceReview(consultation.getId());

        return OmniDimCallResponse.builder()
                .reviewId(reviewId)
                .patientPhone(consultation.getPatient().getUser().getPhone())
                .build();
    }

    // Step 1 - runs inside a transaction (called through transactionTemplate).
    // Counts this attempt and builds the request body. Patient, doctor and user
    // are lazy, so they must be read here, while the transaction is open.
    private Map<String, Object> recordAttemptAndBuildBody(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (review.getCallAttempts() == null) {
            review.setCallAttempts(1);
        } else {
            review.setCallAttempts(review.getCallAttempts() + 1);
        }
        review.setLastCallAt(LocalDateTime.now());
        reviewRepository.save(review);

        Consultation consultation = review.getConsultation();
        Map<String, Object> callContext = new HashMap<>();
        callContext.put("review_id", review.getId());
        callContext.put("consultation_id", consultation.getId());
        callContext.put("patient_name", consultation.getPatient().getUser().getFirstName() + " "
                + consultation.getPatient().getUser().getLastName());
        callContext.put("doctor_name", consultation.getDoctor().getUser().getFirstName() + " "
                + consultation.getDoctor().getUser().getLastName());

        Map<String, Object> body = new HashMap<>();
        body.put("agent_id", omniDimProperties.getAgentId());
        body.put("to_number", "+91" + consultation.getPatient().getUser().getPhone());
        body.put("call_context", callContext);
        return body;
    }

    // Step 3 - runs inside a transaction (called through transactionTemplate)
    private void saveCallId(Long reviewId, String omnidimCallId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        review.setOmnidimCallId(omnidimCallId);
        reviewRepository.save(review);
    }

    // Places one call. Every attempt is counted, even a failed one, so the
    // retry job can never loop forever.
    private boolean placeCall(Long reviewId) {
        // Step 1: short transaction
        Map<String, Object> body = transactionTemplate.execute(status -> recordAttemptAndBuildBody(reviewId));

        if (!StringUtils.hasText(omniDimProperties.getApiKey())) {
            log.warn("Omnidim Api key isnt configured");
            return false;
        }

        // Step 2: the HTTP call, with no transaction open
        OmniDimCallResponse callResponse = callOmnidim(reviewId, body);

        // Step 3: short transaction
        if (callResponse != null && callResponse.getOmnidimCallId() != null) {
            String omnidimCallId = callResponse.getOmnidimCallId();
            transactionTemplate.executeWithoutResult(status -> saveCallId(reviewId, omnidimCallId));
        }
        return callResponse != null;
    }

    private OmniDimCallResponse callOmnidim(Long reviewId, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(omniDimProperties.getApiKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            OmniDimCallResponse response = restTemplate.postForObject(
                    omniDimProperties.getBaseUrl() + "/calls/dispatch", entity, OmniDimCallResponse.class);
            log.info("OmniDim dispatch response for review {}: {}", reviewId, response);
            return response;
        } catch (RestClientException ex) {
            log.error("Failed to trigger OmniDim voice call for review {}: {}", reviewId, ex.getMessage());
            return null;
        }
    }

    @Override
    public OmniDimCallResponse triggerVoiceReview(String email, VoiceReviewTriggerRequest request) {
        // short transaction: validate and create the PENDING review
        OmniDimCallResponse response = transactionTemplate.execute(
                status -> validateAndCreateReview(email, request.getConsultationId()));

        boolean callPlaced = placeCall(response.getReviewId());

        // read the review again to pick up the call id stored in step 3
        Review review = reviewRepository.findById(response.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", response.getReviewId()));

        response.setOmnidimCallId(review.getOmnidimCallId());
        response.setStatus(callPlaced ? review.getStatus().name() : "NOT_TRIGGERED");
        return response;
    }

    @Override
    public void scheduleCallsForCompletedConsultations() {
        LocalDateTime cutOff = LocalDateTime.now().minusDays(voiceReviewProperties.getInitialDelayDays());
        List<Consultation> dueConsultations = consultationRepository
                .findByStatusAndUpdatedAtBefore(Consultation.Status.COMPLETED, cutOff);

        for (Consultation consultation : dueConsultations) {
            if (reviewRepository.existsByConsultationId(consultation.getId())) {
                continue;
            }
            Long consultationId = consultation.getId();
            Long reviewId = transactionTemplate.execute(status -> createPendingVoiceReview(consultationId));
            placeCall(reviewId);
            log.info("Scheduler created voice review {} for consultation {} (completed before {})",
                    reviewId, consultationId, cutOff);
        }
    }

    @Override
    public void retryUnansweredCalls() {
        LocalDateTime retryCutOff = LocalDateTime.now().minusHours(voiceReviewProperties.getRetryGapHours());
        List<Review> reviews = reviewRepository.findByStatusAndSourceAndCallAttemptsLessThan(
                Review.Status.PENDING, Review.Source.VOICE, voiceReviewProperties.getMaxAttempts());

        for (Review review : reviews) {
            if (review.getLastCallAt() != null && review.getLastCallAt().isAfter(retryCutOff)) {
                continue;
            }
            placeCall(review.getId());
            log.info("Scheduler retried voice review {} (max {} attempts)",
                    review.getId(), voiceReviewProperties.getMaxAttempts());
        }
    }

}
