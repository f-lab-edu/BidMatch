package com.project.bidmatch.common.exception;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ErrorResponse.of(
            errorCode.getStatus().value(),
            ex.getMessage()
        ));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    ErrorCode errorCode = ErrorCode.INVALID_INPUT;
    FieldError fieldError = ex.getBindingResult().getFieldError();
    String message = fieldError != null ? fieldError.getDefaultMessage() : errorCode.getMessage();
    return ResponseEntity.status(errorCode.getStatus())
        .body(ErrorResponse.of(
            errorCode.getStatus().value(),
            message
        ));
  }
}
