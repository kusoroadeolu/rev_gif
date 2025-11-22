package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.exceptions.GifPersistenceException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.kusoroadeolu.revgif.enums.GifEntityFields.*;
import static com.github.kusoroadeolu.revgif.enums.GifEntityFields.SEARCH_QUERY;
import static com.github.kusoroadeolu.revgif.enums.GifEntityFields.TENOR_ID;
import static com.github.kusoroadeolu.revgif.enums.GifEntityFields.TENOR_URL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GifCommandServiceImplTest {

    private Frame testFrame;
    private Gif testGif;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private LogMapper logMapper;

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private GifCommandServiceImpl commandService;


    @BeforeEach
    public void setup(){
        testFrame = Frame.builder()
                .pHash(12345L)
                .frameIdx(1)
                .nmHammingDist(0.5)
                .build();

        testGif = Gif.builder()
                .tenorId("tenor-123")
                .tenorUrl("http://tenor.com/view/123")
                .mimeType("image/gif")
                .description("Funny cat")
                .searchQuery("cat")
                .frames(Set.of(testFrame)) // Mutable list for the service to modify
                .build();

        // Lenient stubbing for logging to avoid cluttering strict stubs
        lenient().when(logMapper.log(any(String.class), any(String.class))).thenReturn("");
    }

    @Test
    void shouldSuccessfullyBatchSave() {
        List<Gif> inputGifs = List.of(testGif);
        long generatedId = 1L;
        Gif savedGifFromDb = Gif.builder()
                .id(generatedId)
                .tenorId(testGif.getTenorId())
                .build();

        //Simulating key holder population
        doAnswer(invocationOnMock -> {
            KeyHolder k = invocationOnMock.getArgument(2);
            k.getKeyList().add(Map.of("id", generatedId));
            return new int[]{1};
        }).when(this.jdbcTemplate).batchUpdate(any(PreparedStatementCreator.class), any(BatchPreparedStatementSetter.class), any(KeyHolder.class));

        when(this.namedParameterJdbcTemplate.query(anyString(), any(Map.class), any(RowMapper.class))).thenReturn(List.of(savedGifFromDb));

        //Act
        this.commandService.batchSave(inputGifs);


        //Assert
        ArgumentCaptor<List<Frame>> f = ArgumentCaptor.forClass(List.class);
        verify(jdbcTemplate, times(1)).batchUpdate(anyString(), f.capture(), eq(1), any());
        List<Frame> captured = f.getValue();
        assertEquals(1, captured.size());
        assertEquals(captured.getFirst().getId(), this.testFrame.getId());
    }

    @Test
    void shouldNotSaveFrames_whenNoGeneratedIdsReturned() {
        List<Gif> inputGifs = List.of(testGif);

        //Act
        this.commandService.batchSave(inputGifs);

        //Assert
        verify(this.jdbcTemplate, times(1)).batchUpdate(any(PreparedStatementCreator.class), any(BatchPreparedStatementSetter.class), any(KeyHolder.class));
        verify(this.jdbcTemplate, never()).batchUpdate(anyString(), anyList(), eq(1), any());
        verify(this.namedParameterJdbcTemplate, never()).query(anyString(), anyMap(), any(RowMapper.class));

    }

    @Test
    void shouldThrowGifPersistenceException_onGenericException() {
        List<Gif> inputGifs = List.of(testGif);
        var runtimeException = new RuntimeException();
        when(this.jdbcTemplate.batchUpdate(any(PreparedStatementCreator.class), any(BatchPreparedStatementSetter.class), any(KeyHolder.class)))
                .thenThrow(runtimeException);

        //Act & Assert
        var ex = assertThrows(GifPersistenceException.class, () -> {
            this.commandService.batchSave(inputGifs);
        });
        assertEquals(runtimeException, ex.getCause());
        assertEquals("An error occurred during batch save", ex.getMessage());

    }
}