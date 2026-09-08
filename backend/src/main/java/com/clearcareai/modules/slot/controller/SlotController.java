package com.clearcareai.modules.slot.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clearcareai.common.ApiResponse;
import com.clearcareai.modules.slot.dto.SlotRequestDto;
import com.clearcareai.modules.slot.dto.SlotResponseDto;
import com.clearcareai.modules.slot.service.SlotService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
;



@RestController 
@RequestMapping("/api/slots")
@RequiredArgsConstructor 
public class SlotController {
    private final SlotService slotService;
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<SlotResponseDto> create(@RequestBody @Valid SlotRequestDto slot,Authentication authentication) {
      SlotResponseDto response = slotService.createSlot(authentication.getName(), slot);
      
      return ApiResponse.success("Slot created", response);
  }
  @GetMapping("/doctor/{doctorId}")
  public ApiResponse<List<SlotResponseDto>> getSlotsByDoctor(@PathVariable Long doctorId, @RequestParam (required = false) String dayOfWeek) {
        List<SlotResponseDto> response =  slotService.getSlotsByDoctor(doctorId, dayOfWeek);
        return ApiResponse.success("Slots fetched successfully", response);  
  }
  @DeleteMapping("/{id}")
   public ApiResponse<Void> delete(@PathVariable Long id,Authentication authentication){
        slotService.deleteSlot(authentication.getName(), id);
      return ApiResponse.success("slot deleted ", null);
   }
  
  
}
