package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.events.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.events.GifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.repos.FrameRepository;
import dev.brachtendorf.jimagehash.hash.Hash;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class GifQueryServiceImplTest {

    @Mock
    private FrameRepository frameRepository;

    @Mock
    private AppConfigProperties appConfigProperties;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private GifQueryServiceImpl queryService;



    @Test
    void shouldCallEventPublisher_whenGifsAreFoundInDb() {
        //Arrange
        Hash hash = new Hash(new BigInteger("123456"), 1, 1);
        FrameWrapper frameWrapper = new FrameWrapper(0, null, "png");
        HashWrapper hw = new HashWrapper(frameWrapper, hash);
        String session = "session";
        List<GifSearchCompletedEvent> list = List.of(new GifSearchCompletedEvent("dummyurl", "desc"));

        when(this.appConfigProperties.nmHammingThreshold()).thenReturn(0.35);
        when(this.frameRepository.compareByHash(hw.hash().getHashValue().longValue(), 0.35))
                .thenReturn(list);

        //Act
        BatchGifSearchCompletedEvent event = this.queryService.findGifsFromDb(hw, session);

        //Assert
        assertNotNull(event);
        assertEquals(list.getFirst().description(), event.completedEventList().getFirst().description());
        assertEquals(list.getFirst().tenorUrl(), event.completedEventList().getFirst().tenorUrl());
        verify(this.eventPublisher, times(1)).publishEvent(event);
    }

    @Test
    void shouldNotCallEventPublisher_whenGifsAreFoundInDb() {
        //Arrange
        Hash hash = new Hash(new BigInteger("567891"), 1, 1);
        FrameWrapper frameWrapper = new FrameWrapper(0, null, "png");
        HashWrapper hw = new HashWrapper(frameWrapper, hash);
        String session = "session";
        List<GifSearchCompletedEvent> list = List.of();

        when(this.appConfigProperties.nmHammingThreshold()).thenReturn(0.35);
        when(this.frameRepository.compareByHash(hw.hash().getHashValue().longValue(), 0.35))
                .thenReturn(list);

        //Act
        BatchGifSearchCompletedEvent event = this.queryService.findGifsFromDb(hw, session);

        //Assert
        assertNotNull(event);
        assertTrue(event.completedEventList().isEmpty());
        verify(this.eventPublisher, times(0)).publishEvent(any());
    }
}