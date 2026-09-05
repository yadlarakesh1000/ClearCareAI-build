package com.clearcareai.modules.patient.serviceimpl;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.patient.dto.PatientRequestDto;
import com.clearcareai.modules.patient.dto.PatientResponseDto;
import com.clearcareai.modules.patient.entity.Patient;
import com.clearcareai.modules.patient.mapper.PatientMapper;
import com.clearcareai.modules.patient.repository.PatientRepository;
import com.clearcareai.modules.patient.service.PatientService;
import com.clearcareai.modules.patient.validator.PatientValidator;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
           private final PatientRepository patientRepository;
           private final UserRepository userRepository;
           private final PatientMapper patientMapper;
           private final PatientValidator patientValidator;
           private User getUserByEmail(String email){
                 Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new ResourceNotFoundException("User", "email", email);
    }


  @Override
  @Transactional
  public PatientResponseDto createProfile(String email, PatientRequestDto dto) {
             User user = getUserByEmail(email);
         boolean isExists = patientRepository.existsByUserId(user.getId());
           patientValidator.validateProfileDoesNotExist(isExists);
           Patient patient = patientMapper.toEntity(dto);
           patient.setUser(user);
           Patient saved = patientRepository.save(patient);
           log.info("Created patient profile for user: {}", email);

           return patientMapper.toResponseDto(saved);


  }

  @Override
  @Transactional
  public PatientResponseDto updateProfile(String email, PatientRequestDto dto) {
           User user =getUserByEmail(email);
           Optional<Patient> optionalPatient = patientRepository.findByUserId(user.getId());
           if(!optionalPatient.isPresent()){
             throw new ResourceNotFoundException("Patient profile not found for user: " + email);
           }
           Patient patient = optionalPatient.get();
           patientMapper.updateEntityFromDto(dto, patient);
           Patient saved = patientRepository.save(patient);
           return patientMapper.toResponseDto(saved);
  }

  @Override
  public PatientResponseDto getMyProfile(String email) {
              User user =getUserByEmail(email);
           Optional<Patient> patient=   patientRepository.findByUserId(user.getId());
           if(!patient.isPresent()){
               throw new ResourceNotFoundException("Patient profile not found for user:" + email);
           }
         return  patientMapper.toResponseDto(patient.get());
  }

  @Override
  public PatientResponseDto getPatientById(Long id) {
       Optional<Patient> optionalPatient = patientRepository.findById(id);
        if (!optionalPatient.isPresent()) {
            throw new ResourceNotFoundException("Patient", "id", id);
        }
       

        return patientMapper.toResponseDto(optionalPatient.get());
    }

  }
  

