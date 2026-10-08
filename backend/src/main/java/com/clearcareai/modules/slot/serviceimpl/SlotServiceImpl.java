package com.clearcareai.modules.slot.serviceimpl;


import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.Appointment.Repository.AppointmentRepository;
import com.clearcareai.modules.Appointment.entity.Appointment;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.slot.dto.SlotRequestDto;
import com.clearcareai.modules.slot.dto.SlotResponseDto;
import com.clearcareai.modules.slot.entity.Slot;
import com.clearcareai.modules.slot.exception.SlotException;
import com.clearcareai.modules.slot.mapper.SlotMapper;
import com.clearcareai.modules.slot.repository.SlotRepository;
import com.clearcareai.modules.slot.service.SlotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotServiceImpl implements SlotService {
  private static final long MIN_DURATION_MINUTES = 15;
  private static final long MAX_DURATION_MINUTES = 60;
  private final AppointmentRepository appointmentRepository;
  private final SlotRepository slotRepository;
  private final DoctorRepository doctorRepository;
  private final SlotMapper slotMapper;
  private final UserRepository userRepository;
  @Override
  @Transactional
  public SlotResponseDto createSlot(String email, SlotRequestDto dto) {
                Doctor doctor = getDoctorByEmail(email);
                DayOfWeek dayOfWeek = DayOfWeek.valueOf(dto.getDayOfWeek());
                if(!dto.getStartTime().isBefore(dto.getEndTime())){
                  throw new SlotException("start time must be before end time");
                }
            long durationMinutes = Duration.between(dto.getStartTime(),dto.getEndTime()).toMinutes();
            if(durationMinutes<MIN_DURATION_MINUTES || durationMinutes>MAX_DURATION_MINUTES){
               throw new SlotException("Slot duration must be between 15 and 60 minutes");
            }
            List<Slot> existingSlots = slotRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctor.getId(), dayOfWeek);
            boolean overlaps = false;
            for(Slot existing: existingSlots){
              boolean newStartsBeforeExistingEnds = dto.getStartTime().isBefore(existing.getEndTime());
              boolean existingStartBeforeNewEnds = existing.getStartTime().isBefore(dto.getEndTime());
              if(newStartsBeforeExistingEnds && existingStartBeforeNewEnds){
                overlaps=true;
                break;
              }
            }
            if(overlaps){
               throw new SlotException("Slot overlaps with an existing slot for this day");
        }
        Slot slot = slotMapper.toEntity(dto);
        slot.setDoctor(doctor);
        slot.setDayOfWeek(dayOfWeek);
        slot.setIsActive(true);
        Slot saved = slotRepository.save(slot);
        return slotMapper.toResponseDto(saved);

  }
  @Override
  public List<SlotResponseDto> getSlotsByDoctor(Long doctorId, String dayOfWeek) {
    List<Slot>slots;
    if(dayOfWeek !=null && !dayOfWeek.isBlank()){
      slots= slotRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, DayOfWeek.valueOf(dayOfWeek));
    }
    else{
      slots = slotRepository.findByDoctorIdAndIsActiveTrue(doctorId);
    }
    List<SlotResponseDto> response = new ArrayList<>();
    for(Slot slot:slots){
      response.add(slotMapper.toResponseDto(slot));
    }
    return response;
  }
  @Override
  @Transactional
  public void deleteSlot(String email, Long id) {
      Doctor doctor = getDoctorByEmail(email);
      Optional<Slot> optional = slotRepository.findById(id);
      if(optional.isEmpty()){
        throw new ResourceNotFoundException("slot","id",id);
      }
      Slot slot = optional.get();
      if(!slot.getDoctor().getId().equals(doctor.getId())){
         throw new AccessDeniedException("You can only delete your own slots");
        }
        slot.setIsActive(false);
        slotRepository.save(slot);
           log.info("Deactivated slot {} for doctor {}", id, doctor.getId());
    }
  
  private Doctor getDoctorByEmail(String email){
       Optional<User> userOptional = userRepository.findByEmail(email);
       if(userOptional.isEmpty()){
        throw new ResourceNotFoundException("User","email",email);
       }
       User user = userOptional.get();
       Optional<Doctor> doctorOptional = doctorRepository.findByUserId(user.getId());
       if(doctorOptional.isEmpty()){
          throw new ResourceNotFoundException("Doctor Profile not found for user: "+email);
       }
       Doctor doctor = doctorOptional.get();
       return doctor;
  }
	@Override
	public List<SlotResponseDto> getAvailableSlots(String email, Long doctorId, LocalDate date) {
		    DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<Slot> slots = slotRepository.findByDoctorIdAndDayOfWeekAndIsActiveTrue(doctorId, dayOfWeek);
        List<Appointment> appointmentsOnDate = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
        Set<Long> activelyBookedSlotIds = new HashSet<>();
        for(Appointment appointment : appointmentsOnDate){
           if(Boolean.TRUE.equals(appointment.getActiveBooking())){
            activelyBookedSlotIds.add(appointment.getSlot().getId());
           }
        }
        List<SlotResponseDto> available = new ArrayList<>();
        for(Slot slot:slots){
          if(!activelyBookedSlotIds.contains(slot.getId())){
            available.add(slotMapper.toResponseDto(slot));
          }
        }
        return  available;
	}
  
}
