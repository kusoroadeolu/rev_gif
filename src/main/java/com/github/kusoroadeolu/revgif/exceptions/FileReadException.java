package com.github.kusoroadeolu.revgif.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileReadException extends RuntimeException {
    public FileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileReadException(String message) {
        super(message);
    }
}
