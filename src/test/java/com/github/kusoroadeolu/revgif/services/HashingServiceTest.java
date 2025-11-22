package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HashingServiceTest {

    private List<FrameWrapper> testWrapper;
    @Mock
    private HashingAlgorithm hasher;
    @Mock
    private LogMapper logMapper;
    @InjectMocks
    private HashingService hashingService;

    @BeforeEach
    public void setup(){

        FrameWrapper STANDARD_FRAME = new FrameWrapper(
                0,
                createTestImage(100, 100),
                "gif"
        );

         FrameWrapper MIDDLE_FRAME = new FrameWrapper(
                5,
                createTestImage(50, 75),
                "gif"
        );


         testWrapper = List.of(STANDARD_FRAME, MIDDLE_FRAME);
         lenient().when(logMapper.log(anyString(), anyString())).thenReturn("");
    }


    @Test
    public void shouldHashFrames(){
        Hash h = mock(Hash.class);
        Hash h2 = mock(Hash.class);
        when(hasher.hash(testWrapper.getFirst().image())).thenReturn(h);
        when(hasher.hash(testWrapper.get(1).image())).thenReturn(h2);

        //Act
        List<HashWrapper> hw = this.hashingService.hashFrames(testWrapper);

        //Assert
        assertNotNull(hw);
        assertEquals(2, hw.size());
        assertEquals(h, hw.getFirst().hash());
        assertEquals(h2, hw.get(1).hash());

    }


    private BufferedImage createTestImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

}