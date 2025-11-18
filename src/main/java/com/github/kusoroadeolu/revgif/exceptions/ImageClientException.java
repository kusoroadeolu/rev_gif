package com.github.kusoroadeolu.revgif.exceptions;


public class ImageClientException extends RuntimeException {
    public ImageClientException(String message) {
        super(message);
    }

    public ImageClientException() {
        super();
    }

    public ImageClientException(Throwable cause) {
        super(cause);
    }
}
