package com.github.kusoroadeolu.revgif.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileReadException.class)
    public ResponseEntity<ApiError> handleServerExceptions(FileReadException e){
        final ApiError err = new ApiError(e.statusCode(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatusCode.valueOf(e.statusCode()));
    }

    @ExceptionHandler(UnsupportedFileFormatException.class)
    public ResponseEntity<ApiError> handleBadRequestExceptions(UnsupportedFileFormatException e){
        final ApiError err = new ApiError(e.statusCode(), e.getMessage(), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatusCode.valueOf(e.statusCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericExceptions(Exception e){
        final ApiError err = new ApiError(500, "An unexpected error occurred.", LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
