package com.github.kusoroadeolu.revgif.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedFileFormatException extends RuntimeException {

    private final int statusCode;

    public UnsupportedFileFormatException(String message) {
        this.statusCode = 400;
        super(message);
    }

    public UnsupportedFileFormatException(String message, Throwable cause) {
        this.statusCode = 400;
        super(message, cause);
    }

    public int statusCode() {
        return statusCode;
    }
}
