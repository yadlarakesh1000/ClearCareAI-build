package com.clearcareai.modules.patient.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.clearcareai.common.ApiResponse;
import com.clearcareai.modules.patient.dto.PatientRequestDto;
import com.clearcareai.modules.patient.dto.PatientResponseDto;
import com.clearcareai.modules.patient.service.PatientService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;




@RestController
@RequestMapping("api/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
  @PostMapping("/profile")
  public ResponseEntity<ApiResponse<PatientResponseDto>> createprofile(@Valid @RequestBody PatientRequestDto dto,Authentication authenitcation) {
              
         PatientResponseDto response = patientService.createProfile(authenitcation.getName(), dto);
                URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/api/patients/{id}")
                .buildAndExpand(response.getId())
                .toUri();
          return ResponseEntity.created(location).body(ApiResponse.success("Patient profile is created", response));

  }
  
  @GetMapping("/profile")
  public ApiResponse<PatientResponseDto> getMyProfile(Authentication authentication) {
         PatientResponseDto response = patientService.getMyProfile(authentication.getName());
         
         return ApiResponse.success("Profile fetched",response);
          }
  @GetMapping("/{id}")
  public ApiResponse<PatientResponseDto> getPatientByid(@PathVariable Long id) {
       PatientResponseDto response = patientService.getPatientById(id);
       return ApiResponse.success("Patient details fetched", response);
  }

  @PutMapping("/profile")
  public ApiResponse<PatientResponseDto> updateProfile(@Valid @RequestBody PatientRequestDto dto,Authentication authentication) {
    PatientResponseDto response = patientService.updateProfile(authentication.getName(),dto);
   
      
      return ApiResponse.success("Pateint profile updated successfully", response);
  }
  
}
