package com.clearcareai.modules.consultation.serviceimpl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.Appointment.Repository.AppointmentRepository;
import com.clearcareai.modules.Appointment.entity.Appointment;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.consultation.dto.ConsultationRequestDto;
import com.clearcareai.modules.consultation.dto.ConsultationResponseDto;
import com.clearcareai.modules.consultation.entity.Consultation;
import com.clearcareai.modules.consultation.exception.ConsultationException;
import com.clearcareai.modules.consultation.mapper.ConsultationMapper;
import com.clearcareai.modules.consultation.repository.ConsultationRepository;
import com.clearcareai.modules.consultation.service.ConsultationService;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.repository.DoctorRepository;


import org.springframework.security.access.AccessDeniedException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Service 
@RequiredArgsConstructor 
@Slf4j 
public class ConsultationServiceImpli implements ConsultationService {
    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationMapper consultationMapper;
    private final DoctorRepository doctorRepository;
    private  final UserRepository userRepository;
    private Doctor getDoctorByEmail(String email){
          Optional<User> userOptional = userRepository.findByEmail(email);
          if(!userOptional.isPresent()){
             throw new ConsultationException("User not found");
          }
          User user = userOptional.get();
         Optional<Doctor> doctor = doctorRepository.findByUserId(user.getId());
         if(!doctor.isPresent()){
              throw new ResourceNotFoundException("Doctor profile not found for user");
         }
         return doctor.get();
          
    }
    
    
    @Transactional 
    @Override
  public ConsultationResponseDto startConsultation(String email, ConsultationRequestDto request) {
      if (request.getAppointmentId() == null) {
            throw new ConsultationException("Appointment ID is required");
        }
        Doctor doctor = getDoctorByEmail(email);
        Optional<Appointment> appointmentOptional  = appointmentRepository.findById(request.getAppointmentId());
        if(!appointmentOptional.isPresent()){
          throw new ResourceNotFoundException("Appointment","Id",request.getAppointmentId());
        }
        Appointment appointment = appointmentOptional.get();
        if(!appointment.getDoctor().getId().equals(doctor.getId())){
           throw new AccessDeniedException("You are not authorized to start consultation");
        }
        if(appointment.getStatus()!=Appointment.Status.BOOKED){
                     throw new ConsultationException("Consultation only be created for only a Booked Appointment");
        }
        if (consultationRepository.existsByAppointmemtId(appointment.getId())) {
             throw new ConsultationException("Consultation already exist for this appointment");
        }
        Consultation consultation = Consultation.builder().appointment(appointment).doctor(doctor).patient(appointment.getPatient()).status(Consultation.Status.IN_PROGRESS).build();
        Consultation saved = consultationRepository.save(consultation);
        log.info("Consultation for this id{} started",request.getAppointmentId());
       return  consultationMapper.tOResponseDto(saved);

  }
 
  @Transactional 
  @Override
  public ConsultationResponseDto updateConsultation(String email, Long id, ConsultationRequestDto request) {
       Doctor doctor = getDoctorByEmail(email);
       Optional<Consultation> consOptional=consultationRepository.findById(id);
       if(!consOptional.isPresent()){
        throw new ResourceNotFoundException("Consultation not found");
       }
       Consultation consultation = consOptional.get();
       if(!consultation.getDoctor().getId().equals(doctor.getId())){
         throw new AccessDeniedException("You are not allowed to update this consultation");
       }
       if(StringUtils.hasText(request.getDiagnosis())){
            consultation.setDiagnosis(request.getDiagnosis());
       }
       if(StringUtils.hasText(request.getNotes())){
        consultation.setNotes(request.getNotes());
       }
       if(StringUtils.hasText(request.getPrescription())){
        consultation.setPrescription(request.getPrescription());
       }
       Consultation saved = consultationRepository.save(consultation);
       log.info("Consultation updated{}",id);
       return consultationMapper.tOResponseDto(saved);
  }

 
 
 
 
 
 
 
 
 
  @Transactional 
  @Override
  public ConsultationResponseDto completeConsultation(String email, Long id) {
                   Doctor doctor = getDoctorByEmail(email);
                   Optional<Consultation> consultationOptional = consultationRepository.findById(id);
                   if(!consultationOptional.isPresent()){
                    throw new ResourceNotFoundException("Consultation not found ");
                   }
                   Consultation consultation = consultationOptional.get();
                   if(!consultation.getDoctor().getId().equals(doctor.getId())){
                    throw new AccessDeniedException("You are not allowed to complete this consultation");
                   }
                   consultation.setStatus(Consultation.Status.COMPLETED);
                   Consultation saved = consultationRepository.save(consultation);
                  Appointment appointment =consultation.getAppointment();
                  appointment.setStatus(Appointment.Status.COMPLETED);
                  appointmentRepository.save(appointment);
                  log.info("Completed consultation {} and appointment {}",id,appointment.getId());
                  return consultationMapper.tOResponseDto(saved);

  }


  @Override
  public ConsultationResponseDto getConsultationById(String email, Long id) {
         Optional<Consultation> consultationOptional =consultationRepository.findById(id);
         if(!consultationOptional.isPresent()){
          throw new ResourceNotFoundException("Consultation","id",id);
         }
         Consultation consultation = consultationOptional.get();
         validateViewAccess(email,consultation);
         return  consultationMapper.tOResponseDto(consultation);

  }
  private void validateViewAccess(String email,Consultation consultation){
            User user = userRepository.findByEmail(email).orElseThrow(()->new ResourceNotFoundException("User","email",email));
            boolean isOwnerDoctor = user.getRole()==User.Role.ROLE_DOCTOR
            && consultation.getDoctor().getUser().getId().equals(user.getId());
            boolean isOwnerPatient = user.getRole() == User.Role.ROLE_PATIENT
                && consultation.getPatient().getUser().getId().equals(user.getId());
            boolean isAdmin = user.getRole() == User.Role.ROLE_ADMIN;
            if(!isOwnerDoctor && !isAdmin && !isOwnerPatient){
              throw new AccessDeniedException("You are not authorized to view this consultation");
            }
            
  }

  @Override
  public ConsultationResponseDto getConsultationByAppointmentId(String email, Long appointmentId) {
          Consultation consultation = consultationRepository.findByAppointmentId(appointmentId).orElseThrow(()-> new ResourceNotFoundException("Consultation","id",appointmentId));
          validateViewAccess(email, consultation);
          return  consultationMapper.tOResponseDto(consultation);
  }
  
}
