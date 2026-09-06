package com.clearcareai.modules.doctor.serviceimpl;

import com.clearcareai.common.PagedResponse;
import com.clearcareai.exception.ResourceNotFoundException;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.doctor.dto.DoctorRequestDto;
import com.clearcareai.modules.doctor.dto.DoctorResponseDto;
import com.clearcareai.modules.doctor.entity.Doctor;
import com.clearcareai.modules.doctor.mapper.DoctorMapper;
import com.clearcareai.modules.doctor.repository.DoctorRepository;
import com.clearcareai.modules.doctor.service.DoctorService;
import com.clearcareai.modules.doctor.validator.DoctorValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceImpl implements DoctorService {

  private final DoctorRepository doctorRepository;
  private final DoctorMapper doctorMapper;
  private final UserRepository userRepository;
  private final DoctorValidator doctorValidator;
  @Override
  @Transactional
  @CacheEvict(allEntries = true,cacheNames="doctors"
  )
  public DoctorResponseDto createProfile(String email, DoctorRequestDto requestDto) {
                  User user = getUserByEmail(email);
                  boolean profileExists = doctorRepository.existsByUserId(user.getId());
                   doctorValidator.validateProfileDoesNotExist(profileExists);
                   Doctor doctor = doctorMapper.toEntity(requestDto);
                   doctor.setUser(user);
                   Doctor saved = doctorRepository.save(doctor);
                   log.info("Created Doctor profile for user:{}",email);
                   return toResponseDtoWithRatings(saved);

  }
 
  @Override
  @Cacheable(value = "doctors")
  public PagedResponse<DoctorResponseDto> getDoctors(String specialization, String search, int page, int size) {
          Pageable pageable = PageRequest.of(page, size);
          Page<Doctor> doctorPage;
          if(StringUtils.hasText(search)){
            doctorPage =doctorRepository.searchDoctors(search, pageable);
          }
          else if(StringUtils.hasText(specialization)){
           doctorPage= doctorRepository.findBySpecializationContainingIgnoreCase(specialization, pageable);
          }
          else{
            doctorPage = doctorRepository.findAll(pageable);
          }
          List<DoctorResponseDto>content = new ArrayList<>();
          for(Doctor doctor :doctorPage.getContent()){
            DoctorResponseDto dto = toResponseDtoWithRatings(doctor);
            content.add(dto);
          }
          return new PagedResponse<>(
            content,
            doctorPage.getNumber(),
            doctorPage.getSize(),
            doctorPage.getTotalElements(),
            doctorPage.getTotalPages(),
            doctorPage.isLast()
          );
          
  }
  @Override
  public DoctorResponseDto getDoctorById(Long id) {
        Optional<Doctor> optional = doctorRepository.findById(id);
            if(!optional.isPresent()){
              throw new ResourceNotFoundException("Doctor","id",id);
            }
            Doctor doctor = optional.get();
            return toResponseDtoWithRatings(doctor);


  }
  @Override
  public DoctorResponseDto getMyProfile(String email) {
          User user = getUserByEmail(email);
          Optional<Doctor> optional = doctorRepository.findByUserId(user.getId());
          if(!optional.isPresent()){
            throw new ResourceNotFoundException("Doctor profile not found for user: "+ email);
          }
          Doctor doctor = optional.get();
           return toResponseDtoWithRatings(doctor);
  }
  @Override
  @Transactional
  @CacheEvict(value = "doctors",allEntries = true)
  public DoctorResponseDto updateProfile(String email, DoctorRequestDto requestDto) {
          User user = getUserByEmail(email);
          Optional<Doctor> optional = doctorRepository.findByUserId(user.getId());
          if(!optional.isPresent()){
            throw new ResourceNotFoundException("Doctor profile not found for user: "+ email);
          }
          Doctor doctor= optional.get();
           doctorMapper.updateEntityFromDto(requestDto, doctor);
           Doctor saved = doctorRepository.save(doctor);
          log.info("Updated doctor profile for user: {}",user);
          return toResponseDtoWithRatings(saved);

  }
    private User getUserByEmail(String email){
                 Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        }
        throw new ResourceNotFoundException("User", "email", email);
    }
     private DoctorResponseDto toResponseDtoWithRatings(Doctor doctor) {
       DoctorResponseDto response = doctorMapper.toResponseDto(doctor);
          response.setAverageRating(0.0);
          response.setTotalReviews(0L);
          return response;

  }
  }
