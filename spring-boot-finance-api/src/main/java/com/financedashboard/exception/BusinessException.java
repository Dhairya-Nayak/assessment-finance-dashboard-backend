package com.financedashboard.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = null;
    }

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
