package com.clearcareai.modules.slot.service;

import java.util.List;

import com.clearcareai.modules.slot.dto.SlotRequestDto;
import com.clearcareai.modules.slot.dto.SlotResponseDto;

public interface SlotService {
     
  SlotResponseDto createSlot(String email,SlotRequestDto dto);
  List<SlotResponseDto> getSlotsByDoctor(Long doctorId,String dayOfWeek);
  void deleteSlot(String email,Long id);
  

}
