package com.example.pytainer.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ScriptMasterException extends RuntimeException {
    private final HttpStatus status;

    public ScriptMasterException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public ScriptMasterException(String message) {
        this(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}