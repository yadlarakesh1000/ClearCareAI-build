package com.clearcareai.modules.analytics.serviceimpl;

import com.clearcareai.modules.analytics.service.AnalyticsService;
import org.springframework.stereotype.Service;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.Appointment.Repository.AppointmentRepository;
import com.clearcareai.modules.review.repository.ReviewRepository;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.review.mapper.ReviewMapper;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.Appointment.entity.Appointment;
import com.clearcareai.modules.review.entity.Review;
import com.clearcareai.modules.analytics.dto.DoctorAnalyticsDto;
import com.clearcareai.modules.analytics.dto.PlatformAnalyticsDto;
import com.clearcareai.modules.review.dto.ReviewResponseDto;
import com.clearcareai.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {
      private final DoctorRepository doctorRepository;
      private final AppointmentRepository appointmentRepository;
      private final ReviewRepository reviewRepository;
      private final UserRepository userRepository;
      private final ReviewMapper reviewMapper;

      @Override
      public DoctorAnalyticsDto getDoctorAnalytics(String email,Long doctorId){
            
                 Doctor doctor = doctorRepository.findById(doctorId).orElseThrow(()-> new ResourceNotFoundException("Doctor", "id", doctorId));
                 User user = userRepository.findByEmail(email).orElseThrow(()-> new ResourceNotFoundException("User", "email", email));
                 if(!doctor.getUser().getId().equals(user.getId())){
                    throw new AccessDeniedException("You can only view your own analytics");
                 }
                 long totalAppointments =appointmentRepository.countByDoctorId(doctorId);
                 long completedAppointments = appointmentRepository.countByDoctorIdAndStatus(doctorId, Appointment.Status.COMPLETED);
                  long cancelledAppointments = appointmentRepository.countByDoctorIdAndStatus(doctorId, Appointment.Status.CANCELLED);
                  long totalReviews = reviewRepository.countByDoctorIdAndStatus(doctorId, Review.Status.COMPLETED);
                  double averageRating = roundToOneDecimal(reviewRepository.findAverageRatingByDoctorId(doctorId));
                   Map<String, Long> sentimentBreakdown = new LinkedHashMap<>();
        sentimentBreakdown.put("positive",
                reviewRepository.countByDoctorIdAndSentiment(doctorId, Review.Sentiment.POSITIVE));
        sentimentBreakdown.put("neutral",
                reviewRepository.countByDoctorIdAndSentiment(doctorId, Review.Sentiment.NEUTRAL));
        sentimentBreakdown.put("negative",
                reviewRepository.countByDoctorIdAndSentiment(doctorId, Review.Sentiment.NEGATIVE));

        // This calendar month's appointment count.
        YearMonth currentMonth = YearMonth.now();
        long monthlyAppointments = appointmentRepository.countByDoctorIdAndAppointmentDateBetween(
                doctorId, currentMonth.atDay(1), currentMonth.atEndOfMonth());

        // Last 6 months, oldest first, for the bar chart.
        Map<String, Long> monthlyBreakdown = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth month = currentMonth.minusMonths(i);
            long count = appointmentRepository.countByDoctorIdAndAppointmentDateBetween(
                    doctorId, month.atDay(1), month.atEndOfMonth());
            monthlyBreakdown.put(month.toString(), count);
        }

        // The 5 most recent reviews, mapped to DTOs with a plain loop.
        List<Review> recent = reviewRepository.findTop5ByDoctorIdOrderByCreatedAtDesc(doctorId);
        List<ReviewResponseDto> recentReviews = new ArrayList<>();
        for (Review review : recent) {
            recentReviews.add(reviewMapper.toResponseDto(review));
        }
        return DoctorAnalyticsDto.builder()
        .totalAppointments(totalAppointments)
        .doctorId(doctor.getId())
        .doctorName(doctor.getUser().getFirstName() +" "+doctor.getUser().getLastName())
        .completedAppointments(completedAppointments)
        .cancelledAppointments(cancelledAppointments)
        .totalReviews(totalReviews)
        .averageRating(averageRating)
        .sentimentBreakdown(sentimentBreakdown)
        .monthlyAppointments(monthlyAppointments)
        .monthlyBreakdown(monthlyBreakdown)
        .recentReviews(recentReviews)
        .build();         
      }

@Override
public PlatformAnalyticsDto getPlatformAnalytics(){
        long totalUsers = userRepository.count();
        long totalAdmins = userRepository.countByRole(User.Role.ROLE_ADMIN);
        long totalDoctors = userRepository.countByRole(User.Role.ROLE_DOCTOR);
        long totalPatients = userRepository.countByRole(User.Role.ROLE_PATIENT);
        
        long totalAppointments =appointmentRepository.count();
        long bookedAppointments = appointmentRepository.countByStatus(Appointment.Status.BOOKED);
        long completedAppointments = appointmentRepository.countByStatus(Appointment.Status.COMPLETED);
        long cancelledAppointments = appointmentRepository.countByStatus(Appointment.Status.CANCELLED);

        long totalReviews = reviewRepository.count();
        double averagePlatformRating = roundToOneDecimal(reviewRepository.findAveragePlatformRating());
        long totalVoiceReviews = reviewRepository.countBySource(Review.Source.VOICE);
        long totalTextReviews = reviewRepository.countBySource(Review.Source.TEXT);
        long totalFlaggedReviews = reviewRepository.countByIsFlaggedTrue();
        
        return PlatformAnalyticsDto.builder()
        .totalUsers(totalUsers)
        .totalAdmins(totalAdmins)
        .totalDoctors(totalDoctors)
        .totalPatients(totalPatients)
        .totalAppointments(totalAppointments)
        .bookedAppointments(bookedAppointments)
        .completedAppointments(completedAppointments)
        .cancelledAppointments(cancelledAppointments)
        .totalReviews(totalReviews)
        .averagePlatformRating(averagePlatformRating)
        .totalVoiceReviews(totalVoiceReviews)
        .totalTextReviews(totalTextReviews)
        .totalFlaggedReviews(totalFlaggedReviews)
        .build();

}
 private double roundToOneDecimal(Double value){
     if(value== null){
        return 0.0;
     }
     return Math.round(value*10.0)/10.0;
 }
}