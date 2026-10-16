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
import org.springframework.transaction.annotation.Transactional;
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

@Service 
@Slf4j 
@RequiredArgsConstructor 
public class VoiceReviewServiceImpl  implements VoiceReviewService{
       private  final ReviewRepository reviewRepository;
       private final ConsultationRepository consultationRepository;
       private final DoctorRepository doctorRepository;
       private final UserRepository userRepository;
       private final RestTemplate restTemplate;
       private final OmniDimProperties omniDimProperties;
       private  final VoiceReviewProperties voiceReviewProperties;


private Doctor getDoctorByEmail(String email){
   User user = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException(email));
   Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseThrow(()->new ResourceNotFoundException(email));
   return doctor;
}

private Review createPendingVoiceReview(Consultation consultation){
       Review review=  Review.builder().consultation(consultation).patient(consultation.getPatient()).doctor(consultation.getDoctor()).status(Review.Status.PENDING).source(Review.Source.VOICE).isFlagged(false).callAttempts(0).build();
         Review saved =  reviewRepository.save(review);
         return saved;
}
private boolean placeCall(Review review){
       if(review.getCallAttempts()==null){
              review.setCallAttempts(1);
       }
       else{
        review.setCallAttempts(review.getCallAttempts()+1);
       }
       review.setLastCallAt(LocalDateTime.now());
       if(!StringUtils.hasText(omniDimProperties.getApiKey())){
                 log.warn("Omnidim Api key isnt configured");
                 return false;
       }
        OmniDimCallResponse callResponse = callOmnidim(review);
        if (callResponse != null && callResponse.getOmnidimCallId() != null) {
            review.setOmnidimCallId(callResponse.getOmnidimCallId());
        }
        reviewRepository.save(review);
        return callResponse != null;
    }
private  OmniDimCallResponse callOmnidim(Review review){
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
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(omniDimProperties.getApiKey());
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try{
          OmniDimCallResponse response = restTemplate.postForObject(omniDimProperties.getBaseUrl() + "/calls/dispatch", entity, OmniDimCallResponse.class);
          log.info("OmniDim dispatch response for review {}: {}", review.getId(), response);
          return response;
        }
        catch(RestClientException ex){
            log.error("Failed to trigger OmniDim voice call for review {}: {}", review.getId(), ex.getMessage());
            return null;
        }
    

}

  @Override
    @Transactional
    public OmniDimCallResponse triggerVoiceReview(String email, VoiceReviewTriggerRequest request) {
        Doctor doctor = getDoctorByEmail(email);

        Consultation consultation = consultationRepository.findById(request.getConsultationId())
                .orElseThrow(() -> new ResourceNotFoundException("Consultation", "id", request.getConsultationId()));

        if (!consultation.getDoctor().getId().equals(doctor.getId())) {
            throw new AccessDeniedException("You are not authorized to trigger a review for this consultation");
        }

        if (consultation.getStatus() != Consultation.Status.COMPLETED) {
            throw new BadRequestException("Voice review can only be triggered for a completed consultation");
        }

        if (reviewRepository.existsByConsultationId(consultation.getId())) {
            throw new BadRequestException("A review already exists for this consultation");
        }

        Review review = createPendingVoiceReview(consultation);
        boolean callPlaced = placeCall(review);

        return OmniDimCallResponse.builder()
                .reviewId(review.getId())
                .omnidimCallId(review.getOmnidimCallId())
                .status(callPlaced ? review.getStatus().name() : "NOT_TRIGGERED")
                .patientPhone(consultation.getPatient().getUser().getPhone())
                .build();
    }
   
























  @Transactional
  @Override
  public void scheduleCallsForCompletedConsultations() {
       LocalDateTime cutOff = LocalDateTime.now().minusDays(voiceReviewProperties.getInitialDelayDays());
       List<Consultation> dueConsultations = consultationRepository.findByStatusAndUpdatedAtBefore(Consultation.Status.COMPLETED, cutOff);
            for(Consultation consultation :dueConsultations){
                 if(reviewRepository.existsByConsultationId(consultation.getId())){
                  continue;
                 }
                 else{
                     Review review = createPendingVoiceReview(consultation);
                     placeCall(review);
                     log.info("Scheduler created voice review {} for consultation {} (completed before {})",
                    review.getId(), consultation.getId(), cutOff);
        }
                 }
            }
  

  @Override
  @Transactional
  public void retryUnansweredCalls() {
       LocalDateTime retryCutOff = LocalDateTime.now().minusHours(voiceReviewProperties.getRetryGapHours());
       List<Review> reviews = reviewRepository.findByStatusAndSourceAndCallAttemptsLessThan(Review.Status.PENDING, Review.Source.VOICE, voiceReviewProperties.getMaxAttempts());
       for(Review review : reviews){
            if(review.getLastCallAt() != null && review.getLastCallAt().isAfter(retryCutOff)){
               continue;
            }
            else{
              placeCall(review);
              log.info("Scheduler retried voice review {} (attempt {}/{})",
                    review.getId(), review.getCallAttempts(), voiceReviewProperties.getMaxAttempts());
            }
       }
  }
  
}
