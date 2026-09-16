package com.clearcareai.modules.Appointment.serviceimpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.clearcareai.common.PagedResponse;
import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.Appointment.Repository.AppointmentRepository;
import com.clearcareai.modules.Appointment.dto.AppointmentRequestDto;
import com.clearcareai.modules.Appointment.dto.AppointmentResponseDto;
import com.clearcareai.modules.Appointment.entity.Appointment;
import com.clearcareai.modules.Appointment.exception.AppointmentException;
import com.clearcareai.modules.Appointment.mapper.AppointmentMapper;
import com.clearcareai.modules.Appointment.service.AppointmentService;
import com.clearcareai.modules.Appointment.validator.AppointmentValidator;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.patient.entity.Patient;
import com.clearcareai.modules.patient.repository.PatientRepository;
import com.clearcareai.modules.slot.entity.Slot;
import com.clearcareai.modules.slot.repository.SlotRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class AppointmentServiceImpl implements  AppointmentService{
     private  final  AppointmentValidator appointmentValidator;
     private  final AppointmentRepository appointmentRepository;
     private final AppointmentMapper appointmentMapper;
     private final PatientRepository patientRepository;
     private final DoctorRepository doctorRepository;
     private final SlotRepository slotRepository;
     private final UserRepository userRepository;
     @Override
     @Transactional 
     public AppointmentResponseDto createAppointment(String email, AppointmentRequestDto dto) {
        Patient patient = getPatientByEmail(email);
        Optional<Doctor> optionalDoctor = doctorRepository.findById(dto.getDoctorId());
        if(optionalDoctor.isEmpty()){
          throw new ResourceNotFoundException("Doctor","id",dto.getDoctorId());
        }
        Doctor doctor = optionalDoctor.get();
        Optional<Slot> optionalSlot = slotRepository.findById(dto.getSlotId());
        if(optionalSlot.isEmpty()){
          throw new ResourceNotFoundException("Slot","id",dto.getSlotId());
        }
        Slot slot = optionalSlot.get();
        if(!slot.getDoctor().getId().equals(doctor.getId())){
          throw new AppointmentException("Slot doesnot belongd to selected doctor");
        }
        appointmentValidator.validateSlotActive(slot);
        appointmentValidator.validateSlotStartIsInFutute(dto.getAppointmentDate(), slot);
        appointmentValidator.validateDayOfWeekMatches(dto.getAppointmentDate(), slot);
        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndSlotIdAndAppointmentDateAndActiveBookingIsTrue(doctor.getId(),slot.getId(),dto.getAppointmentDate());
        appointmentValidator.validateDoctorSlotNotBooked(alreadyBooked);
        List<Appointment> patientAppointmentsOnDate = appointmentRepository.findByPatientIdAndAppointmentDateAndStatus(patient.getId(), dto.getAppointmentDate(), Appointment.Status.BOOKED);
        appointmentValidator.validateNoPatientOverlap(patientAppointmentsOnDate, slot);
        Appointment appointment = appointmentMapper.toEntity(dto);
         appointment.setPatient(patient);
         appointment.setDoctor(doctor);
         appointment.setSlot(slot);
         appointment.setStatus(Appointment.Status.BOOKED);
         appointment.setActiveBooking(Boolean.TRUE);
      Appointment saved  = appointmentRepository.save(appointment);
      log.info("created appointment{} for patient {} with doctor{}",saved.getId(),saved.getPatient(),saved.getDoctor());
      return appointmentMapper.toResponseDto(appointment);
     }
     @Override
     public PagedResponse<AppointmentResponseDto> getMyAppointments(String email, String status, int page, int size) {
           Optional<Patient> optionalPatient = findPatientByEmail(email);
           Pageable pageable= PageRequest.of(page,size);
           if(optionalPatient.isEmpty()){
             return toPagedResponse(Page.empty(pageable));
           }
           Patient patient = optionalPatient.get();
           Page<Appointment> appointmentPage;
           if(StringUtils.hasText(status)){
           appointmentPage = appointmentRepository.findByPatientIdAndStatus(patient.getId(), Appointment.Status.valueOf(status), pageable);
           } 
           else{
            appointmentPage = appointmentRepository.findByPatientId(patient.getId(), pageable);
           }
           return toPagedResponse(appointmentPage);
     }

		 public PagedResponse<AppointmentResponseDto> getDoctorAppointments(String email, String status, LocalDate date,
        int page, int size) {
       Optional<Doctor> doctorOptional = findDoctorByEmail(email);
       Pageable pageable = PageRequest.of(page, size);
       if(doctorOptional.isEmpty()){
        return toPagedResponse(Page.empty(pageable));
       }
       Doctor doctor = doctorOptional.get();
       Page<Appointment> appointmentPage;
       if(StringUtils.hasText(status) && date!=null){
         appointmentPage = appointmentRepository.findByDoctorIdAndStatusAndAppointmentDate(doctor.getId(), Appointment.Status.valueOf(status), date, pageable);

       }
       else if(StringUtils.hasText(status)){
        appointmentPage = appointmentRepository.findByDoctorIdAndStatus(doctor.getId(), Appointment.Status.valueOf(status), pageable);
       }
       else if(date !=null){
         appointmentPage = appointmentRepository.findByDoctorIdAndAppointmentDate(doctor.getId(), date, pageable);
       }
       else{
         appointmentPage = appointmentRepository.findByDoctorId(doctor.getId(), pageable);
       }
       return toPagedResponse(appointmentPage);
     }
     @Override
     public AppointmentResponseDto getAppointmentById(String email, Long id) {
             Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
             if(appointmentOptional.isEmpty()){
              throw new ResourceNotFoundException("Appointment","id",id);
             }
             Appointment appointment = appointmentOptional.get();
             validateViewAccess(email,appointment);
             return appointmentMapper.toResponseDto(appointment);
     }
     @Override
     public AppointmentResponseDto cancelAppointment(String email, Long id) {
           Patient patient = getPatientByEmail(email);
           Optional<Appointment> appointmentOptional = appointmentRepository.findById(id);
           if(appointmentOptional.isEmpty()){
            throw new ResourceNotFoundException("Appointment","id",id);
           }
           Appointment appointment = appointmentOptional.get();
           if(!appointment.getPatient().getId().equals(patient.getId())){
                throw new AccessDeniedException("You can only cancel your own appointments");
           }
           appointmentValidator.validateCancellable(appointment);
           appointment.setStatus(Appointment.Status.CANCELLED);
           appointment.setActiveBooking(null);
           Appointment saved = appointmentRepository.save(appointment);
           log.info("Cancelled appointment {} for patient{}",id,patient.getId());
           return appointmentMapper.toResponseDto(saved);
     }
     @Override
     public PagedResponse<AppointmentResponseDto> getAllAppointments(int page, int size) {
             Pageable pageable = PageRequest.of(page, size);
            Page<Appointment> appointmentPage = appointmentRepository.findAll(pageable);
            return  toPagedResponse(appointmentPage); 
     }
    private  void validateViewAccess(String email,Appointment appointment){
       User user = getUserByEmail(email);
       boolean isOwnerPatient = user.getRole()==User.Role.ROLE_PATIENT 
                                  &&appointment.getPatient().getUser().getId().equals(user.getId());
        boolean isOwnerDoctor = user.getRole()==User.Role.ROLE_DOCTOR 
                                  &&appointment.getDoctor().getUser().getId().equals(user.getId());  
        boolean isAdmin = user.getRole()==User.Role.ROLE_ADMIN;
        if(!isOwnerDoctor && !isOwnerPatient && !isAdmin){
          throw new AccessDeniedException("You are not authorized to view this appointment");
        }
    }
    private  Patient getPatientByEmail(String email){
       User user =  getUserByEmail(email);
       Optional<Patient> patientOptional =  patientRepository.findByUserId(user.getId());
       if(patientOptional.isEmpty()){
        throw new ResourceNotFoundException("Patient profile not found for user: "+email);
       }
       return patientOptional.get();

    }
    private User getUserByEmail(String email){
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()){
           throw new ResourceNotFoundException("User","email",email);
        }
        return userOptional.get();
    }
    private Optional<Patient> findPatientByEmail(String email){
       User user = getUserByEmail(email);
       return patientRepository.findByUserId(user.getId());
    }
    private PagedResponse<AppointmentResponseDto> toPagedResponse(Page<Appointment> appointmentPage) {
			    List<AppointmentResponseDto> content = new ArrayList<>();
          for(Appointment appointment:appointmentPage.getContent()){
              content.add(appointmentMapper.toResponseDto(appointment));
          }
          return new PagedResponse<>(
              content,
              appointmentPage.getNumber(),
              appointmentPage.getSize(),
              appointmentPage.getTotalElements(),
              appointmentPage.getTotalPages(),
              appointmentPage.isLast()
          );
		}
    private Optional<Doctor> findDoctorByEmail(String email){
         User user = getUserByEmail(email);
         return doctorRepository.findByUserId(user.getId());
    }   
}
