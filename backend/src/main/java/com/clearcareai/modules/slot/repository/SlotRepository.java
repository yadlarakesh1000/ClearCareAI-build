package com.clearcareai.modules.slot.repository;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.clearcareai.modules.slot.entity.Slot;

public interface SlotRepository extends JpaRepository<Slot,Long> {
    List<Slot> findByDoctorId(Long doctorId);
    List<Slot> findByDoctorIdAndDayOfWeek(Long doctorid, DayOfWeek dayOfWeek);
    List<Slot> findByDoctorIdAndIsActiveTrue(Long doctorId);
    List<Slot> findByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId,DayOfWeek dayOfWeek);

}
