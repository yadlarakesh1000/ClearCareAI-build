package com.clearcareai.modules.voicereview.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor 
@NoArgsConstructor 
@Data 
@Builder 
@JsonIgnoreProperties (ignoreUnknown = true)
public class OmniDimCallResponse {
      private Long reviewId;

      @JsonProperty("call_id")
     @JsonAlias({"id", "callId", "call_request_id", "callRequestId", "requestId", "request_id"})
      private String omnidimCallId;
      private String status;
      private String patientPhone; 
}
