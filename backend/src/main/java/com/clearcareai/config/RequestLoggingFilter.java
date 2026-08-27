package com.clearcareai.config;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException,IOException{
       long startTime = System.currentTimeMillis();
       try{
        filterChain.doFilter(request, response);
       }
       finally{
        long duration = System.currentTimeMillis()-startTime;
        log.info("{} {} -> {} ({} ms)",request.getMethod(),request.getRequestURI(),response.getStatus(),duration);
       }
      }
       @Override
       protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException
       {
         String path = request.getRequestURI();

         return path.startsWith("/actuator")
         || path.startsWith("/swagger-ui")
         || path.startsWith("/api-docs");
       }

  }
  

