package com.clearcareai.modules.consultation.exception;

import com.clearcareai.exception.BadRequestException;

public class ConsultationException extends BadRequestException {
       ConsultationException(String message){
          super(message);
       }
}
