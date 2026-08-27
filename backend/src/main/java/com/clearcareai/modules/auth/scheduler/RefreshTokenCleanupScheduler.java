package com.clearcareai.modules.auth.scheduler;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.clearcareai.modules.auth.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

  private final RefreshTokenRepository refreshTokenRepository;

  @Scheduled(cron = "${app.auth.refresh-token-cleanup-cron:0 0 3 * * *}")
  @Transactional
  public void refreshCleanUp(){
     try{
      LocalDateTime cutoff = LocalDateTime.now();
     int deletedCount= refreshTokenRepository.deleteByExpiryDateBefore(cutoff);
        if(deletedCount>0) {
          log.info("deleted {} counts",deletedCount);
        }
     }
     catch(Exception ex){
       log.error("Refresh token cleanup failed: {}", ex.getMessage(), ex);
     }

  }
  
}
