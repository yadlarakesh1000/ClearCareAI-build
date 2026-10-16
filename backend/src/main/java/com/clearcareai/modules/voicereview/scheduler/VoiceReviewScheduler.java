package com.clearcareai.modules.voicereview.scheduler;
import org.springframework.scheduling.annotation.Scheduled;
import com.clearcareai.modules.voicereview.service.VoiceReviewService;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Component
@Slf4j
@RequiredArgsConstructor

public class VoiceReviewScheduler {
      
      private final VoiceReviewService  voiceReviewService;
      
      @Scheduled(fixedDelayString ="${app.voicereview.check-interval-ms}")
      public void runVoiceServiceCycle(){
                   try{
                    voiceReviewService.scheduleCallsForCompletedConsultations();
                    voiceReviewService.retryUnansweredCalls();
                   }
                   catch(Exception ex){
                       log.error("Voice Review Cycle failed:{}", ex.getMessage(),ex);
                   }

      }



}



