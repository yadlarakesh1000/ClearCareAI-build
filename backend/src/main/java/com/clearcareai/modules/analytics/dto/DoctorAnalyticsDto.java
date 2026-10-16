package com.clearcareai.modules.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.List;
import com.clearcareai.modules.review.dto.ReviewResponseDto;
// response shape of 1 doctors analytics 
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorAnalyticsDto{
    private Long doctorId;
    private String doctorName;
    private long totalAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long totalReviews;
    private double averageRating;
    private Map<String,Long> sentimentBreakDown;
    private long monthlyAppointments;
    private Map<String,Long> monthlyBreakdown;
    private List<ReviewResponseDto> recentReviews;
    

}