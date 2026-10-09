package com.clearcareai.modules.consultation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.clearcareai.common.ApiResponse;
import com.clearcareai.modules.consultation.dto.ConsultationRequestDto;
import com.clearcareai.modules.consultation.dto.ConsultationResponseDto;
import com.clearcareai.modules.consultation.service.ConsultationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;



@RestController 
@RequiredArgsConstructor 
@RequestMapping("api/consultation")
public class ConsultationController {
 private final ConsultationService consultationService;

  @PostMapping()
  public ResponseEntity<ApiResponse<ConsultationResponseDto>>start(@RequestBody @Valid ConsultationRequestDto request,Authentication authentication) {
      
      ConsultationResponseDto response = consultationService.startConsultation(authentication.getName(), request);
      URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
      .buildAndExpand(response.getId())
      .toUri();
      return ResponseEntity.created(location).body(ApiResponse.success("Consultation started", response));
  
  }
  
  
    @PutMapping("/{id}/complete")
    public ApiResponse<ConsultationResponseDto> completeConsultation(@PathVariable Long id, Authentication authentication) {
        ConsultationResponseDto response = consultationService.completeConsultation(authentication.getName(), id);
        return ApiResponse.success("Consultation completed", response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ConsultationResponseDto> getConsultationById(@PathVariable Long id, Authentication authentication) {
        ConsultationResponseDto response = consultationService.getConsultationById(authentication.getName(), id);
        return ApiResponse.success("Consultation retrieved", response);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ApiResponse<ConsultationResponseDto> getConsultationByAppointmentId(@PathVariable Long appointmentId,Authentication authentication) {
        ConsultationResponseDto response =
                consultationService.getConsultationByAppointmentId(authentication.getName(), appointmentId);
        return ApiResponse.success("Consultation retrieved", response);
    }
}
