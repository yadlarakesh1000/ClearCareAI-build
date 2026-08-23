package com.clearcareai.modules.auth.service;

import com.clearcareai.modules.auth.dto.AuthResponse;
import com.clearcareai.modules.auth.dto.LoginRequest;
import com.clearcareai.modules.auth.dto.RegisterRequest;
import com.clearcareai.modules.auth.entity.RefreshToken;

public interface AuthService {
  

  public AuthResponse login(LoginRequest request);
  public AuthResponse register(RegisterRequest request);
  public AuthResponse refreshToken(RefreshToken request);

}
