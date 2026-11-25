package com.clearcareai.modules.voicereview;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import com.clearcareai.modules.appointment.repository.AppointmentRepository;
import com.clearcareai.modules.appointment.entity.Appointment;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.consultation.entity.Consultation;
import com.clearcareai.modules.consultation.repository.ConsultationRepository;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.patient.entity.Patient;
import com.clearcareai.modules.patient.repository.PatientRepository;
import com.clearcareai.modules.review.entity.Review;
import com.clearcareai.modules.review.repository.ReviewRepository;
import com.clearcareai.modules.slot.entity.Slot;
import com.clearcareai.modules.slot.repository.SlotRepository;
import com.clearcareai.modules.voicereview.dto.OmniDimCallResponse;
import com.clearcareai.modules.voicereview.service.VoiceReviewService;

// Runs the scheduler path the way the real scheduler does: on a plain thread,
// with no web request and no outer transaction. That is the case where lazy
// loading breaks if the code reads patient/doctor data outside a transaction.
@SpringBootTest
@ActiveProfiles("test")
class VoiceReviewServiceImplTest {

    @Autowired private VoiceReviewService voiceReviewService;
    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private SlotRepository slotRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ConsultationRepository consultationRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    // the real RestTemplate would call OmniDim over the internet
    @MockBean private RestTemplate restTemplate;

    @Test
    void scheduler_createsPendingReview_countsAttempt_andStoresCallId() {
        Consultation consultation = createCompletedConsultation("sched");
        makeConsultationOld(consultation.getId());

        when(restTemplate.postForObject(anyString(), any(), eq(OmniDimCallResponse.class)))
                .thenReturn(OmniDimCallResponse.builder().omnidimCallId("5090001").build());

        voiceReviewService.scheduleCallsForCompletedConsultations();

        Optional<Review> saved = reviewRepository.findByConsultationId(consultation.getId());
        assertTrue(saved.isPresent());
        assertEquals(Review.Status.PENDING, saved.get().getStatus());
        assertEquals(Review.Source.VOICE, saved.get().getSource());
        assertEquals(1, saved.get().getCallAttempts());
        assertEquals("5090001", saved.get().getOmnidimCallId());
    }

    @Test
    void failedCall_stillCountsTheAttempt() {
        Consultation consultation = createCompletedConsultation("fail");
        makeConsultationOld(consultation.getId());

        // OmniDim gave no usable answer
        when(restTemplate.postForObject(anyString(), any(), eq(OmniDimCallResponse.class)))
                .thenReturn(null);

        voiceReviewService.scheduleCallsForCompletedConsultations();

        Review saved = reviewRepository.findByConsultationId(consultation.getId()).orElseThrow();
        assertEquals(1, saved.getCallAttempts());
        assertEquals(null, saved.getOmnidimCallId());
    }

    // @UpdateTimestamp always writes "now", so push the date back with plain SQL
    private void makeConsultationOld(Long consultationId) {
        jdbcTemplate.update("UPDATE consultations SET updated_at = ? WHERE id = ?",
                LocalDateTime.now().minusDays(30), consultationId);
    }

    private Consultation createCompletedConsultation(String tag) {
        User patientUser = userRepository.save(User.builder()
                .firstName("Asha").lastName("Patient")
                .email(tag + "-patient@test.com").password("x").phone("9000000001")
                .role(User.Role.ROLE_PATIENT).build());
        User doctorUser = userRepository.save(User.builder()
                .firstName("Ravi").lastName("Doctor")
                .email(tag + "-doctor@test.com").password("x").phone("9000000002")
                .role(User.Role.ROLE_DOCTOR).build());

        Patient patient = patientRepository.save(Patient.builder()
                .user(patientUser).dateofBirth(LocalDate.of(2000, 1, 1))
                .gender(Patient.Gender.FEMALE).build());
        Doctor doctor = doctorRepository.save(Doctor.builder()
                .user(doctorUser).specialization("General").qualification("MBBS")
                .experienceYears(5).consultationFee(new BigDecimal("500.00")).build());

        Slot slot = slotRepository.save(Slot.builder()
                .doctor(doctor).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(10, 30)).build());

        Appointment appointment = appointmentRepository.save(Appointment.builder()
                .patient(patient).doctor(doctor).slot(slot)
                .appointmentDate(LocalDate.now().minusDays(31))
                .status(Appointment.Status.COMPLETED).activeBooking(true).build());

        return consultationRepository.save(Consultation.builder()
                .appointment(appointment).doctor(doctor).patient(patient)
                .status(Consultation.Status.COMPLETED).build());
    }
}
