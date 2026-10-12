package com.clearcareai.modules.review.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.clearcareai.modules.review.dto.ReviewResponseDto;
import com.clearcareai.modules.review.entity.Review;

@Mapper (componentModel = "spring")
public interface ReviewMapper {

   @Mapping (target="consultationId",source="consultation.id")
   @Mapping (target = "patientId",source="patient.id")
   @Mapping(target = "doctorId",source="doctor.id")
   @Mapping (target="patientName",expression = "java(review.getPatient().getUser().getFirstName()+\"\" + review.getPatient().getUser().getLatName())")
   @Mapping (target = "doctorName",expression = "java(review.getDoctor().getUser().getFirstName()+\"\" + review.getDoctor().getUser().getLatName())")
  public ReviewResponseDto toResponseDto(Review review);
  
  
}
