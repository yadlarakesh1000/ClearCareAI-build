package com.clearcareai.modules.auth.service;

import com.clearcareai.modules.auth.dto.AuthResponse;
import com.clearcareai.modules.auth.dto.LoginRequest;
import com.clearcareai.modules.auth.dto.RefreshTokenRequest;
import com.clearcareai.modules.auth.dto.RegisterRequest;


public interface AuthService {
  

  public AuthResponse login(LoginRequest request);
  public AuthResponse register(RegisterRequest request);
  public AuthResponse refreshToken(RefreshTokenRequest request);

}
