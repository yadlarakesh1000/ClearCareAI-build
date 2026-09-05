package com.clearcareai.modules.doctor.validator;

import org.springframework.stereotype.Component;

import com.clearcareai.modules.doctor.exception.DoctorException;

@Component
public class DoctorValidator {
    public void validateProfileDoesNotExist(boolean profileExists){
      if(!profileExists){
        throw new DoctorException("Doctor profile not found");
      }

     }
  
}
