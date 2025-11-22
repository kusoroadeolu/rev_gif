package com.github.kusoroadeolu.revgif.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileReadException extends RuntimeException {

    private final int statusCode;

    public FileReadException(String message, Throwable cause) {
        this.statusCode = 500;
        super(message, cause);
    }

    public int statusCode() {
        return statusCode;
    }
}
