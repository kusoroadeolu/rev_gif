package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.GeminiConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.exceptions.ImageClientException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.google.genai.Client;
import com.google.genai.Models;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import dev.brachtendorf.jimagehash.hash.Hash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiImageClientTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GeminiModelsProvider geminiModelsProvider;

    @Mock
    private GeminiConfigProperties geminiConfigProperties;
    @Mock
    private LogMapper logMapper;
    @InjectMocks
    private GeminiImageClient imageClient;


    @BeforeEach
    public void setup(){
        lenient().when(this.logMapper.log(anyString(), anyString())).thenReturn("");

    }

    @Test
    void shouldGetFrameDescription() {
        //Arrange
        Hash hash = new Hash(new BigInteger("123456"), 1, 1);
        FrameWrapper frameWrapper = new FrameWrapper(0, new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY), "png");
        HashWrapper hw = new HashWrapper(frameWrapper, hash);
        GenerateContentResponse response = mock(GenerateContentResponse.class);

        when(this.geminiConfigProperties.model()).thenReturn("gemini");
        when(this.geminiModelsProvider.models().generateContent(anyString(), any(Content.class), any(GenerateContentConfig.class)))
                .thenAnswer(e -> response);
        when(response.text()).thenReturn("gif description");

        //Act
        ImageClientResponse resp = this.imageClient.getFrameDescription(hw);

        //Assert
        assertNotNull(resp);
        assertEquals("gif description", resp.searchQuery());
        assertEquals(hash, resp.hash());
    }

    @Test
    void getFrameDescription_shouldThrowImageClientException_onNullText() {
        //Arrange
        Hash hash = new Hash(new BigInteger("123456"), 1, 1);
        FrameWrapper frameWrapper = new FrameWrapper(0, new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY), "png");
        HashWrapper hw = new HashWrapper(frameWrapper, hash);
        GenerateContentResponse response = mock(GenerateContentResponse.class);

        when(this.geminiConfigProperties.model()).thenReturn("gemini");
        when(this.geminiModelsProvider.models().generateContent(anyString(), any(Content.class), any(GenerateContentConfig.class)))
                .thenAnswer(e -> response);
        when(response.text()).thenReturn(null);

        //Act & Assert
        assertThrows(ImageClientException.class, () -> this.imageClient.getFrameDescription(hw));
    }

    @Test
    void getFrameDescription_shouldThrowImageClientException_onEmptyText() {
        //Arrange
        Hash hash = new Hash(new BigInteger("123456"), 1, 1);
        FrameWrapper frameWrapper = new FrameWrapper(0, new BufferedImage(10, 10, BufferedImage.TYPE_BYTE_GRAY), "png");
        HashWrapper hw = new HashWrapper(frameWrapper, hash);
        GenerateContentResponse response = mock(GenerateContentResponse.class);

        when(this.geminiConfigProperties.model()).thenReturn("gemini");
        when(this.geminiModelsProvider.models().generateContent(anyString(), any(Content.class), any(GenerateContentConfig.class)))
                .thenAnswer(e -> response);
        when(response.text()).thenReturn("");

        //Act & Assert
        assertThrows(ImageClientException.class, () -> this.imageClient.getFrameDescription(hw));
    }



}