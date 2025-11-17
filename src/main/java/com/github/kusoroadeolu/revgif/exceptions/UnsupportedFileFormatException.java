package com.github.kusoroadeolu.revgif.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedFileFormatException extends RuntimeException {
    public UnsupportedFileFormatException(String message) {
        super(message);
    }

    public UnsupportedFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
