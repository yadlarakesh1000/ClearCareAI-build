package com.clearcareai.modules.Appointment.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.clearcareai.modules.Appointment.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment,Long>{
      Page<Appointment> findByPatientId(Long patientId,Pageable pageable);
      Page<Appointment> findByPatientIdAndStatus(Long patientId,Appointment.Status status,Pageable pageable);
       Page<Appointment> findByDoctorId(Long doctorId,Pageable pageable);
       Page<Appointment> findByDoctorIdAndStatus(Long doctorId,Appointment.Status status,Pageable pageable);
      Page<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId,LocalDate appointmentDate,Pageable pageable);
      Page<Appointment> findByDoctorIdAndStatusAndAppointmentDate(Long doctorId,Appointment.Status status,LocalDate appointmentDate,Pageable pageable);
      List<Appointment> findByPatientIdAndAppointmentDateAndStatus(Long patientId,LocalDate appointmentDate,Appointment.Status status);
      List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId,LocalDate appointmentDate);
     boolean existsByDoctorIdAndSlotIdAndAppointmentDateAndActiveBookingIsTrue(Long doctorId,Long slotId,LocalDate appointmentDate); 
}
