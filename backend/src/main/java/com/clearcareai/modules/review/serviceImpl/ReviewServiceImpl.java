package com.clearcareai.modules.review.serviceImpl;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.clearcareai.common.PagedResponse;
import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.consultation.entity.Consultation;
import com.clearcareai.modules.consultation.repository.ConsultationRepository;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.patient.entity.Patient;
import com.clearcareai.modules.patient.repository.PatientRepository;
import com.clearcareai.modules.review.dto.ReviewFlagRequest;
import com.clearcareai.modules.review.dto.ReviewProcessRequest;
import com.clearcareai.modules.review.dto.ReviewRequestDto;
import com.clearcareai.modules.review.dto.ReviewResponseDto;
import com.clearcareai.modules.review.entity.Review;
import com.clearcareai.modules.review.exception.ReviewException;
import com.clearcareai.modules.review.mapper.ReviewMapper;
import com.clearcareai.modules.review.repository.ReviewRepository;
import com.clearcareai.modules.review.service.ReviewService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service 
@Slf4j 
@RequiredArgsConstructor 
public class ReviewServiceImpl implements ReviewService {
       private final ReviewRepository reviewRepository;
       private final DoctorRepository doctorRepository;
       private final UserRepository userRepository;
       private final PatientRepository patientRepository;
       private final ConsultationRepository consultationRepository;
       private final ReviewMapper reviewMapper;
  
        
       private User getUserByEmail(String email){
                 Optional<User> userOptional = userRepository.findByEmail(email);
          if(!userOptional.isPresent()){
                throw new ResourceNotFoundException("User", "email", email);
          }
              User user = userOptional.get();
              return user;
       }

       private Patient getPatientByEmail(String email){
             User user = getUserByEmail(email);
              Patient patient = patientRepository.findByUserId(user.getId()).orElseThrow(()-> new ResourceNotFoundException("Patient profile not found for user: " + email));
              return  patient;
       }
 
 
 




  @Transactional 
  @Override
  public ReviewResponseDto createTextReview(String email, ReviewRequestDto request) {
              Patient patient = getPatientByEmail(email);
              Consultation consultation = consultationRepository.findById(request.getConsultationId()).orElseThrow(()-> new ResourceNotFoundException("Consultation", "id", request.getConsultationId()));
              if(!consultation.getPatient().getId().equals(patient.getId())){
                    throw new AccessDeniedException("You can only review your own consultations");
              }
              if(consultation.getStatus() != Consultation.Status.COMPLETED){
                throw new ReviewException("A review can only be submitted for a completed consultation");
              }
              Review existing = reviewRepository.findByConsultationId(consultation.getId()).orElse(null);
              if(existing !=null){
                 if(existing.getSource()==Review.Source.VOICE && existing.getStatus()==Review.Status.PENDING){
                     existing.setRawTranscript(request.getRawTranscript());
                     existing.setRating(request.getRating());
                     existing.setSource(Review.Source.TEXT);
                     existing.setStatus(Review.Status.COMPLETED);
                     log.info("converted");
                  Review converted=   reviewRepository.save(existing);
                  return reviewMapper.toResponseDto(converted);
                 }
                 else{
                  throw new ReviewException("A review already exists for this consultation");
                 } 
              }
              
                Review newReview = Review.builder().
                                   consultation(consultation)
                                   .patient(patient)
                                   .doctor(consultation.getDoctor())
                                   .rawTranscript(request.getRawTranscript())
                                   .rating(request.getRating())
                                   .status(Review.Status.COMPLETED)
                                   .source(Review.Source.TEXT)
                                   .isFlagged(false)
                                   .build();

             Review saved = reviewRepository.save(newReview);
             return reviewMapper.toResponseDto(saved);

                                     
  }

  @Override
  public PagedResponse<ReviewResponseDto> getReviewsByDoctor(Long doctorId, int page, int size) {
         
    Pageable pageable = PageRequest.of(page,size);
    Page<Review> reviewPage = reviewRepository.findByDoctorId(doctorId, pageable);
      return toPagedResponse(reviewPage);
  }



     private PagedResponse<ReviewResponseDto> toPagedResponse(Page<Review> reviewPage) {
        List<ReviewResponseDto> content = new ArrayList<>();
        for (Review review : reviewPage.getContent()) {
            content.add(reviewMapper.toResponseDto(review));
        }
        return new PagedResponse<>(
                content,
                reviewPage.getNumber(),
                reviewPage.getSize(),
                reviewPage.getTotalElements(),
                reviewPage.getTotalPages(),
                reviewPage.isLast()
        );
    }
  @Override
  public PagedResponse<ReviewResponseDto> getMyReviews(String email, int page, int size) {
     Optional<Patient> patientOpt = findPatientByEmail(email);
        if (patientOpt.isEmpty()) {
            return new PagedResponse<>(new ArrayList<>(), page, size, 0L, 0, true);
        }
         Patient patient = patientOpt.get();
         Pageable pageable = PageRequest.of(page,size);
         Page<Review> review = reviewRepository.findByPatientId(patient.getId(), pageable);
         return toPagedResponse(review);
  }

   private Optional<Patient> findPatientByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        return patientRepository.findByUserId(userOpt.get().getId());
    }

  @Override
  public ReviewResponseDto getReviewById(String email, Long id) {
            Review review =reviewRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("review","id",id));
            validateViewAccess(email,review);
            
            return reviewMapper.toResponseDto(review);
  }

    private void validateViewAccess(String email, Review review) {
        User user = getUserByEmail(email);

        boolean isOwnerPatient = user.getRole() == User.Role.ROLE_PATIENT
                && review.getPatient().getUser().getId().equals(user.getId());
        boolean isOwnerDoctor = user.getRole() == User.Role.ROLE_DOCTOR
                && review.getDoctor().getUser().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == User.Role.ROLE_ADMIN;

        if (!isOwnerPatient && !isOwnerDoctor && !isAdmin) {
            throw new AccessDeniedException("You are not authorized to view this review");
        }
    }


  @Transactional
  @Override
  public ReviewResponseDto processReview(Long id, ReviewProcessRequest request) {
                Review review = reviewRepository.findById(id).orElseThrow(()->new ResourceNotFoundException(""));
                if(review.getStatus() !=Review.Status.PENDING){
                  throw new ReviewException("Only a PENDING review can be processed — this review is already ");
                }
                if(StringUtils.hasText(request.getCleanedReview())){
                      review.setCleanedReview(request.getCleanedReview());
                }
                if (StringUtils.hasText(request.getImprovementSuggestions())) {
            review.setImprovementSuggestions(request.getImprovementSuggestions());
                 }
                if (StringUtils.hasText(request.getSummary())) {
                review.setSummary(request.getSummary());
                                                              }
                if(StringUtils.hasText(request.getPatientFeedback())){
                  review.setPatientFeedback(request.getPatientFeedback());
                }
                if (StringUtils.hasText(request.getSentiment())) {
            review.setSentiment(Review.Sentiment.valueOf(request.getSentiment()));
                                                                }
                if(StringUtils.hasText(request.getRecovered())){
                  review.setRecovered(Review.TriState.valueOf(request.getRecovered()));
                }
          
                
                if(StringUtils.hasText(request.getRecommendDoctor())){
                  review.setRecommendDoctor(Review.TriState.valueOf(request.getRecommendDoctor()));
                }
                if(request.getRating() !=null){
                    review.setRating(request.getRating());
                }
                review.setStatus(Review.Status.COMPLETED);
                Review saved = reviewRepository.save(review);
                log.info("review succesfully processed");
                return reviewMapper.toResponseDto(saved);      
        }

  @Transactional 
  @Override
  public ReviewResponseDto flagReview(String email, Long id, ReviewFlagRequest request) {
         User user = getUserByEmail(email);
     Doctor doctor = doctorRepository.findByUserId(user.getId()).orElseThrow(()-> new ResourceNotFoundException("Doctor profile not found for user: " + email));

     Review review = reviewRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Review", "id", id));
     if(!review.getDoctor().getId().equals(doctor.getId())){
      throw new AccessDeniedException ("You can only flag reviews for your own consultations");
     }
     if(review.getStatus() != Review.Status.COMPLETED){
      throw new ReviewException("only Completed Reviews can be flagged");
     }
       review.setIsFlagged(true);
       review.setFlagReason(request.getFlagReason());
       review.setFlaggedAt(LocalDateTime.now());
       Review saved = reviewRepository.save(review);
        log.info("Flagged review {} by doctor {}", id, doctor.getId());
       return reviewMapper.toResponseDto(saved);
  }

  @Override
  public ReviewResponseDto getReviewByCallId(String callId) {
              Review review = reviewRepository.findByOmnidimCallId(callId).orElseThrow(()->new ResourceNotFoundException("Review not found for call id: " + callId));
              return reviewMapper.toResponseDto(review);
  }
  
}
