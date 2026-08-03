package com.pradip.banksphere.exception;

public class AccountIsNotEnabledException extends RuntimeException {
    public AccountIsNotEnabledException(String message) {
        super(message);

    }
}
