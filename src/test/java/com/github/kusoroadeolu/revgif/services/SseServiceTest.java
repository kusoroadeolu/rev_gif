package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.events.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.events.GifEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.SseWrapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.model.Gif;
import com.github.kusoroadeolu.revgif.model.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SseServiceTest {

    @Mock
    private Map<String, SseWrapper> sseWrappers;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private RedisTemplate<String, Session> sseTemplate;

    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private SseService sseService;

    @BeforeEach
    public void setup(){
        lenient().when(this.logMapper.log(anyString(), anyString())).thenReturn("");
    }

    @Test
    public void shouldSuccessfullyCreateEmitter(){
        //Arrange
        String session = "session";

        //Act
        SseEmitter emitter = this.sseService.createEmitter(session);

        //Assert
        assertNotNull(emitter);
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(this.sseTemplate.opsForValue(), times(1)).set(captor.capture(), any(Session.class), anyLong(), any(TimeUnit.class));
        String val = captor.getValue();
        assertEquals("session:session" , val);
    }


    @Test
    public void shouldSuccessfullyUpdateExpectedEvents(){
        //Arrange
        String session = "session";
        int events = 2;
        SseWrapper wrapper = new SseWrapper(new SseEmitter(Long.MAX_VALUE), null);
        when(this.sseWrappers.get(session)).thenReturn(wrapper);

        //Act
        this.sseService.updateExpectedEvents(session, events);

        //Assert
        assertEquals(events, wrapper.expectedEvents());
    }

    @Test
    public void shouldListenForExpiry(){
        RedisKeyExpiredEvent<Session> key = mock(RedisKeyExpiredEvent.class);
        int events = 2;
        SseWrapper wrapper = new SseWrapper(new SseEmitter(Long.MAX_VALUE), null);
        wrapper.setExpectedEvents(events);
        wrapper.increment();
        wrapper.increment(); //Incr twice
        when(key.getId()).thenReturn("session".getBytes(StandardCharsets.UTF_8));
        when(this.sseWrappers.get(anyString())).thenReturn(wrapper);

        //Act
        this.sseService.listenForSseExpiry(key);

        //Assert
        assertTrue(wrapper.isCompleted());
        verify(this.sseWrappers, times(1)).remove(anyString());

    }

    @Test
    public void shouldDoNothing_ifKeyIsEmpty(){
        RedisKeyExpiredEvent<Session> key = mock(RedisKeyExpiredEvent.class);
        when(key.getId()).thenReturn("".getBytes(StandardCharsets.UTF_8));

        //Act
        this.sseService.listenForSseExpiry(key);

        //Assert
        verify(this.sseWrappers, never()).get(anyString());
        verify(this.sseWrappers, never()).remove(anyString());

    }

    @Test
    public void shouldListenForExpiry_andCleanupIfReceivedEventsIsLessThanExpected(){
        RedisKeyExpiredEvent<Session> key = mock(RedisKeyExpiredEvent.class);
        int events = 2;
        SseWrapper wrapper = new SseWrapper(new SseEmitter(Long.MAX_VALUE), null);
        wrapper.setExpectedEvents(events);
        wrapper.increment();

        //Assert
        assertFalse(wrapper.isCompleted());

        when(key.getId()).thenReturn("session".getBytes(StandardCharsets.UTF_8));
        when(this.sseWrappers.get(anyString())).thenReturn(wrapper);

        //Act
        this.sseService.listenForSseExpiry(key);

        //Assert
        assertTrue(wrapper.isCompleted());
        verify(this.sseWrappers, times(1)).remove(anyString());

    }

}
