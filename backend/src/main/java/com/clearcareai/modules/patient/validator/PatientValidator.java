package com.clearcareai.modules.patient.validator;

import org.springframework.stereotype.Component;

import com.clearcareai.modules.patient.exception.PatientException;

@Component
public class PatientValidator {
     public void validateProfileDoesNotExist(boolean profileExists){
      if(profileExists){
        throw new PatientException("Patient already existed");
      }

     }
   
}
