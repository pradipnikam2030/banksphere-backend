package com.pradip.banksphere.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleEmailException(EmailAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.builder().
                        timestamp(LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error("Conflict")
                        .message(ex.getMessage())
                        .build());

    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiError> handleRoleException(RoleNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.builder().
                        timestamp(LocalDateTime.now())
                        .status(HttpStatus.CONFLICT.value())
                        .error("Conflict")
                        .message(ex.getMessage())
                        .build());

    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidUserException(InvalidCredentialsException ex){
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(ApiError.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.NOT_ACCEPTABLE.value())
                        .error("Not Accepted")
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(AccountIsNotEnabledException.class)
    public ResponseEntity<ApiError> handleAccountIsNotEnabledException(AccountIsNotEnabledException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiError.builder()
                        .timestamp(LocalDateTime.now())
                        .status(HttpStatus.FORBIDDEN.value())
                        .error("FORBIDDEN")
                        .message(ex.getMessage())
                        .build());
    }
}
