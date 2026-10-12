package com.clearcareai.modules.review.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.clearcareai.modules.review.entity.Review;



public interface ReviewRepository  extends  JpaRepository<Review,Long>{
  
     Optional<Review> findByConsultationId(Long consultationId);
     boolean exiexistsByConsultationId(Long consultationId);
     Page<Review> findByPatientId(Long patientId, Pageable pageable);
     Page<Review> findByDoctorId(Long doctorId,Pageable pageable);

     Optional<Review> findByOmnidimCallId(String omnidimCallId);
     List<Review> findByStatusAndSourceAndCallAttemptsLessThan(Review.Status status, Review.Source source, int maxAttempts);

     Page<Review> findByIsFlaggedTrue(Pageable pageable);
     long coucountByIsFlaggedTrue();
     long countByDoctorIdAndSentiment(Long doctorId,Review.Sentiment sentiment);
     long countBySource(Review.Source source);
     List<Review> findTop5ByDoctorIdAndOrderByCreatedAtDesc(Long doctorId);

     @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId AND r.status = 'COMPLETED'")
    Double findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.status = 'COMPLETED'")
    Double findAveragePlatformRating();

}
