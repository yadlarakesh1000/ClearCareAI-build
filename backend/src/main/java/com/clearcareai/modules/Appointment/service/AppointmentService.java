package com.clearcareai.modules.Appointment.service;

import java.time.LocalDate;

import com.clearcareai.common.PagedResponse;
import com.clearcareai.modules.Appointment.dto.AppointmentRequestDto;
import com.clearcareai.modules.Appointment.dto.AppointmentResponseDto;

public interface AppointmentService {


    AppointmentResponseDto createAppointment(String email,AppointmentRequestDto dto);
    PagedResponse<AppointmentResponseDto> getMyAppointments(String email,String status,int page,int size);
    PagedResponse<AppointmentResponseDto> getDoctorAppointments(String email,String status,LocalDate date,int page,int size);
    AppointmentResponseDto getAppointmentById(String email,Long id);
    AppointmentResponseDto cancelAppointment(String email,Long id);
    PagedResponse<AppointmentResponseDto> getAllAppointments(int page,int size);
}
