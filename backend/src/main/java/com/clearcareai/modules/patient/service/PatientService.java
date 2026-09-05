package com.clearcareai.modules.patient.service;

import com.clearcareai.modules.patient.dto.PatientRequestDto;
import com.clearcareai.modules.patient.dto.PatientResponseDto;

public interface PatientService {
      
  public PatientResponseDto createProfile(String email,PatientRequestDto dto);
  public PatientResponseDto updateProfile(String email,PatientRequestDto dto);
  public PatientResponseDto getMyProfile(String email);
  public PatientResponseDto getPatientById(Long id);
}
