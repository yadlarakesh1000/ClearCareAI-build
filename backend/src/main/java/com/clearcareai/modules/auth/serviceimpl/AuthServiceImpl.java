package com.clearcareai.modules.auth.serviceimpl;


import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clearcareai.exception.BadRequestException;
import com.clearcareai.exception.UnauthorizedException;
import com.clearcareai.modules.auth.dto.AuthResponse;
import com.clearcareai.modules.auth.dto.LoginRequest;
import com.clearcareai.modules.auth.dto.RefreshTokenRequest;
import com.clearcareai.modules.auth.dto.RegisterRequest;
import com.clearcareai.modules.auth.entity.RefreshToken;
import com.clearcareai.modules.auth.entity.User;
import com.clearcareai.modules.auth.repository.RefreshTokenRepository;
import com.clearcareai.modules.auth.repository.UserRepository;
import com.clearcareai.modules.auth.service.AuthService;
import com.clearcareai.modules.security.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService{
     private final UserRepository userRepository;
     private final RefreshTokenRepository refreshTokenRepository;
     private final PasswordEncoder passwordEncoder;
     private final JwtTokenProvider jwtTokenProvider;
     private final AuthenticationManager authenticationManager;
     private static final String TOKEN_TYPE = "Bearer";



       @Override
       @Transactional
  public AuthResponse register(RegisterRequest request) {
          if(userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("User Already exist with this email: "+ request.getEmail());
          }
          User user = User.builder().email(request.getEmail()).password(passwordEncoder.encode(request.getPassword())).firstName(request.getFirstName()).lastName(request.getLastName()).phone(request.getPhone()).role(User.Role.valueOf(request.getRole())).isActive(true).build();
          User savedUser = userRepository.save(user);
          log.info("user created with {} email id",request.getEmail());
          return buildAuthResponse(savedUser);

  }

  private AuthResponse buildAuthResponse(User savedUser) {
              
           String accessToken =  jwtTokenProvider.generateAccessToken(savedUser.getEmail(), savedUser.getId(), savedUser.getRole().name());
           String refreshToken = createRefreshToken(savedUser);
           return AuthResponse.builder().accessToken(accessToken).refreshToken(refreshToken).tokenType(TOKEN_TYPE).userId(savedUser.getId()).email(savedUser.getEmail()).role(savedUser.getRole().name()).build();


      }

  private String createRefreshToken(User savedUser) {
            String token = UUID.randomUUID().toString();
            long refreshTokenExpirationSeconds = jwtTokenProvider.getRefreshExpirationMs()/1000;
            LocalDateTime expiryDate = LocalDateTime.now().plusSeconds(refreshTokenExpirationSeconds);
            RefreshToken refreshToken = RefreshToken.builder().user(savedUser).token(token).expiryDate(expiryDate).isRevoked(false).build();
            refreshTokenRepository.save(refreshToken);
            return token;
  }

  @Override
  public AuthResponse login(LoginRequest request) {
          try{
             authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
          } catch(BadCredentialsException ex){
             throw new UnauthorizedException("Invalid email or password");
          }
          catch(DisabledException ex){
            throw  new UnauthorizedException("Your account has been deactivated,contact admin");
          }
          Optional<User>userOptional = userRepository.findByEmail(request.getEmail());
          if(!userOptional.isPresent()){
            throw new UnauthorizedException("Invalid email or password");
          }
           User user = userOptional.get();
           return buildAuthResponse(user);
  }


  @Transactional
  @Override
  public AuthResponse refreshToken(RefreshTokenRequest request) {
        Optional<RefreshToken>tokenOptional= refreshTokenRepository.findByToken(request.getRefreshToken());
        if(!tokenOptional.isPresent()){
          throw new UnauthorizedException("Invalid refresh token");
        }
        RefreshToken token = tokenOptional.get();
        if(Boolean.TRUE.equals(token.getIsRevoked())){
              throw new UnauthorizedException("Refresh token has been revoked");
        }
        if(token.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new UnauthorizedException("Refresh token expired");
        }
        token.setIsRevoked(true);
        refreshTokenRepository.save(token);
        User user = token.getUser();
        return buildAuthResponse(user);

  }
  
}
