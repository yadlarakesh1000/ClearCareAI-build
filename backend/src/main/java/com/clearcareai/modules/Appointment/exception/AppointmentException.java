package com.clearcareai.modules.Appointment.exception;

import com.clearcareai.exception.BadRequestException;

public class AppointmentException extends BadRequestException {

  public AppointmentException(String message) {
    super(message);
  }
  
}
