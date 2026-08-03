package com.pradip.banksphere.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApiError {
    private LocalDateTime timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;
}
