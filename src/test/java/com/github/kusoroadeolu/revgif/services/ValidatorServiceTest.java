package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidatorServiceTest {

    @Mock
    private AppConfigProperties configProperties;
    @Mock
    private Tika tika;
    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private ValidatorService validatorService;

    Set<String> valid = Set.of("image/png");
    @BeforeEach
    public void setup(){
        lenient().when(logMapper.log(anyString(), anyString())).thenReturn("");
    }

    @Test
    void shouldSuccessfullyValidateFile() {
        byte[] b = {1, 2, 3, 4 , 5};
        when(this.configProperties.allowedFileFormats()).thenReturn(valid);
        when(tika.detect(b)).thenReturn("image/png");

        //Act
        FileWrapper fileWrapper = this.validatorService.validateFile(b);

        //Assert
        assertNotNull(fileWrapper);
        assertEquals(b, fileWrapper.bytes());
        assertEquals("image/png", fileWrapper.contentType());

    }

    @Test
    void shouldThrow_whenFileIsEmpty() {
        byte[] empty = new byte[0];

        UnsupportedFileFormatException ex = assertThrows(
                UnsupportedFileFormatException.class,
                () -> validatorService.validateFile(empty)
        );

        assertEquals("File cannot be empty", ex.getMessage());
    }

    @Test
    void shouldThrow_whenTikaFails() {
        byte[] b = {1, 2, 3};
        when(configProperties.allowedFileFormats()).thenReturn(valid);
        when(tika.detect(b)).thenThrow(new RuntimeException("boom"));

        UnsupportedFileFormatException ex = assertThrows(
                UnsupportedFileFormatException.class,
                () -> validatorService.validateFile(b)
        );

        assertTrue(ex.getMessage().contains("Could not determine file type"));
        assertNotNull(ex.getCause());
        assertEquals("boom", ex.getCause().getMessage());
    }

    @Test
    void shouldThrow_whenMimeTypeIsNull() {
        byte[] b = {1, 2, 3};
        when(configProperties.allowedFileFormats()).thenReturn(valid);
        when(tika.detect(b)).thenReturn(null);

        UnsupportedFileFormatException ex = assertThrows(
                UnsupportedFileFormatException.class,
                () -> validatorService.validateFile(b)
        );

        assertTrue(ex.getMessage().contains("Invalid file format"));
    }


    @Test
    void shouldThrow_whenMimeTypeIsNotAllowed() {
        byte[] b = {1, 2, 3};
        when(configProperties.allowedFileFormats()).thenReturn(valid);
        when(tika.detect(b)).thenReturn("image/jpeg"); // not allowed

        UnsupportedFileFormatException ex = assertThrows(
                UnsupportedFileFormatException.class,
                () -> validatorService.validateFile(b)
        );

        assertTrue(ex.getMessage().contains("Invalid file format"));
        assertTrue(ex.getMessage().contains("image/jpeg"));
    }



}