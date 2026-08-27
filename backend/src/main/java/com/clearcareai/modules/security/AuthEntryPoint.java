package com.clearcareai.modules.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.clearcareai.common.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class AuthEntryPoint implements AuthenticationEntryPoint {
  private final ObjectMapper objectMapper=new ObjectMapper();
  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException, ServletException {
          log.error("Unauthorized error:{}",authException.getMessage());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
          ApiResponse<Object> apiResponse = ApiResponse.error("Unauthorized: "+ authException.getMessage());
          response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
  }


  
}
