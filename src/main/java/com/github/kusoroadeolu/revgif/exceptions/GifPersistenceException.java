package com.github.kusoroadeolu.revgif.exceptions;

public class GifPersistenceException extends RuntimeException {
    public GifPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public GifPersistenceException(String message) {
        super(message);
    }
}
