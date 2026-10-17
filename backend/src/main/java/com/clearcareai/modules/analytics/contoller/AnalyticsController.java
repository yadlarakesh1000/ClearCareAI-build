package com.clearcareai.modules.analytics.contoller;

import org.springframework.security.core.Authentication;
import com.clearcareai.common.ApiResponse;
import com.clearcareai.modules.analytics.service.AnalyticsService;
import com.clearcareai.modules.analytics.dto.DoctorAnalyticsDto;
import com.clearcareai.modules.analytics.dto.PlatformAnalyticsDto;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

      private final AnalyticsService analyticsService;

      @GetMapping("/doctor")
      public ApiResponse<DoctorAnalyticsDto> getDoctorAnalytics(@PathVariable Long doctorId,Authentication authentication) {
              DoctorAnalyticsDto response  = analyticsService.getDoctorAnalytics(authentication.getName(),doctorId);
               return ApiResponse.success("Doctor analytics fetched successfully",response);
      }
      @GetMapping("/platform")
      public ApiResponse<PlatformAnalyticsDto> getPlatformAnalytics() {
            PlatformAnalyticsDto response = analyticsService.getPlatformAnalytics();
            return ApiResponse.success("Platform analytics fetched successfully",response);
      }
}