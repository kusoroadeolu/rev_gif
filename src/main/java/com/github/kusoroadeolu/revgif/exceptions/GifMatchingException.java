package com.github.kusoroadeolu.revgif.exceptions;

public class GifMatchingException extends RuntimeException {
    public GifMatchingException(String message) {
        super(message);
    }

    public GifMatchingException(Throwable cause) {
        super(cause);
    }
}
