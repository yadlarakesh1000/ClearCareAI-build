package com.clearcareai.modules.doctor.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.clearcareai.common.ApiResponse;
import com.clearcareai.common.AppConstants;
import com.clearcareai.common.PagedResponse;
import com.clearcareai.modules.doctor.dto.DoctorRequestDto;
import com.clearcareai.modules.doctor.dto.DoctorResponseDto;
import com.clearcareai.modules.doctor.service.DoctorService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;




@RestController
@RequestMapping("api/patients")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
  @PostMapping("/profile")
  public ResponseEntity<ApiResponse<DoctorResponseDto>> createprofile(@Valid @RequestBody DoctorRequestDto dto,Authentication authenitcation) {
     DoctorResponseDto response = doctorService.createProfile(authenitcation.getName(), dto);
      URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/patients/{id}")
                .buildAndExpand(response.getId())
                .toUri(); 
                return ResponseEntity.created(location).body(ApiResponse.success("Doctor profile created", response));
  }
  @GetMapping("/profile")
  public ApiResponse<DoctorResponseDto> getprofile(Authentication authentication) {
        DoctorResponseDto response = doctorService.getMyProfile(authentication.getName());
        return ApiResponse.success("Doctor profile fetched successfully", response);
  }
  @GetMapping("/{id}")
  public ApiResponse<DoctorResponseDto> getDoctorById(@PathVariable Long id) {
      DoctorResponseDto response = doctorService.getDoctorById(id);
      return ApiResponse.success("Doctor profile retrived", response);
  }
  @GetMapping
  public ApiResponse<PagedResponse<DoctorResponseDto>> getDoctors(
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size) {
        PagedResponse<DoctorResponseDto> response = doctorService.getDoctors(specialization, search, page, size);
        return ApiResponse.success("doctors retrived", response); 
  }

  @PutMapping("/profile")
  public ApiResponse<DoctorResponseDto> updateProfile(@Valid @RequestBody DoctorRequestDto dto,Authentication authenitcation) {
         DoctorResponseDto response = doctorService.updateProfile(authenitcation.getName(), dto);
         return ApiResponse.success("Doctor profile updated successfully", response);  
}
}