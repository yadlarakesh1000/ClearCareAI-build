package com.clearcareai.modules.Appointment.validator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Component;

import com.clearcareai.modules.Appointment.entity.Appointment;
import com.clearcareai.modules.Appointment.exception.AppointmentException;
import com.clearcareai.modules.slot.entity.Slot;

@Component 
public class AppointmentValidator {
    
 public void validateSlotStartIsInFutute(LocalDate appointmentDate,Slot slot){
        LocalDateTime  slotStartTime = LocalDateTime.of(appointmentDate,slot.getStartTime());
        if(!slotStartTime.isAfter(LocalDateTime.now())){
                 throw new AppointmentException("Appointment only Booked for a future date");
        }
  }
  public void validateSlotActive(Slot slot){
      if(!Boolean.TRUE.equals(slot.getIsActive())){
        throw new AppointmentException("Slot is not active");
      }
  }
  public void validateDayOfWeekMatches(LocalDate appointmentDate,Slot slot){
       if(appointmentDate.getDayOfWeek() != slot.getDayOfWeek()){
         throw new AppointmentException("Appointment date does not match the slot's day of week");
       }
  }
  public void validateDoctorSlotNotBooked(boolean alreadyBooked){
          if(alreadyBooked){
            throw new AppointmentException("this slot is already booked for the selected date");
          }
  }
  public void validateNoPatientOverlap(List<Appointment> existingAppointments, Slot requestedSlot){
     LocalTime start = requestedSlot.getStartTime();
     LocalTime end = requestedSlot.getEndTime();
     boolean overlap = false;
     for(Appointment existing:existingAppointments){
        Slot existingSlot = existing.getSlot();
        boolean startBeforeExistingEnds = start.isBefore(existingSlot.getEndTime());
        boolean existingStartsBeforeRequestedEnds = existingSlot.getStartTime().isBefore(end);
        if(startBeforeExistingEnds && existingStartsBeforeRequestedEnds){
          overlap = true;
          break;
        }
     }
     if(overlap){
      throw new AppointmentException("You already have an appointment at an overlapping time on this date");
     }
  }
  public void validateCancellable(Appointment appointment){
       if(appointment.getStatus()!=Appointment.Status.BOOKED ){
         throw new AppointmentException("Only appointments with status BOOKED can be cancelled");
       }
  }
}
