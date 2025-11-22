package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.exceptions.GifPersistenceException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import com.github.kusoroadeolu.revgif.services.GifCommandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.kusoroadeolu.revgif.enums.GifEntityFields.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GifCommandServiceImpl implements GifCommandService {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final static String CLASS_NAME = GifCommandServiceImpl.class.getSimpleName();
    private final LogMapper logMapper;
    private final static String BATCH_GIF_INSERT = "INSERT INTO gifs(mime_type, description, tenor_id, tenor_url, search_query) VALUES (?,?,?,?,?) ON CONFLICT(tenor_url) DO NOTHING";
    private final static String BATCH_FRAME_INSERT = "INSERT INTO frames(p_hash, frame_idx, nm_hamming_dist ,gifs) VALUES (?, ?, ?, ?)";
    private final static String SAVED_GIF_QUERY = "SELECT * from gifs WHERE id IN (:id)";

    /**
     * This method batch saves gifs and frames. Each gif is mapped to its tenor ID, batch saved and the gifs which were successfully saved are queried back.
     *</br> These requeried gifs are then used to find the frames mapped to each gif. These frames gif fk are populated then batch saved
     * @param gifs The list of gifs to save
     * @throws GifPersistenceException On unexpected error
     * */
    @Transactional
    @Override
    public void batchSave(@NonNull List<Gif> gifs){
        this.logMapper.log(CLASS_NAME, "Current list size at DB call: %s".formatted(gifs.size()));
        if(gifs.isEmpty()) return;

        try{
            final Map<String, Gif> mappedGifs = gifs.stream()
                    .collect(Collectors.toMap(Gif::getTenorId, e -> e));

            final KeyHolder keyHolder = new GeneratedKeyHolder();
            this.batchUpdateGifs(keyHolder, gifs);
            final List<Long> generatedIds = keyHolder
                    .getKeyList()
                    .stream()
                    .map(m -> (Long) m.get(ID.val()))
                    .toList();  //Get all the generated ids for the gifs
            log.info(this.logMapper.log(CLASS_NAME, "%s IDs generated successfully".formatted(generatedIds.size())));

            if(generatedIds.isEmpty()){
                log.info(this.logMapper.log(CLASS_NAME, "No IDs were generated for this gif batch"));
                return;
            }

            //Get the gifs that were saved. The reason for this is cause some gifs might not have saved due to unique constraints
            final List<Gif> savedGifs = this.querySavedGifs(generatedIds);

            final List<Frame> frames = new ArrayList<>();
            for (final Gif savedGif : savedGifs){
                final Gif mappedGif = mappedGifs.get(savedGif.getTenorId());
                mappedGif.getFrames().forEach(e -> {   //Get the frames to saved from the mapped gif
                    e.setGif(savedGif.getId());
                    frames.add(e);
                });
            }

            this.batchUpdateFrames(frames);
            log.info(this.logMapper.log(CLASS_NAME, "Successfully saved all frames to corresponding gifs"));

        }catch (Exception e){
            log.error(this.logMapper.log(CLASS_NAME, "An error occurred during batch save"), e);
            throw new GifPersistenceException("An error occurred during batch save", e); //Rethrow to trigger transaction
        }
    }

    private List<Gif> querySavedGifs(List<Long> generatedIds){
        return this.namedParameterJdbcTemplate.query(
                SAVED_GIF_QUERY,
                Map.of(ID.val() , generatedIds),
                (rs, _) -> {
                    return Gif.builder()
                            .id(rs.getLong(ID.val()))
                            .mimeType(rs.getString(MIME_TYPE.val()))
                            .description(rs.getString(DESCRIPTION.val()))
                            .tenorUrl(rs.getString(TENOR_URL.val()))
                            .tenorId(rs.getString(TENOR_ID.val()))
                            .searchQuery(rs.getString(SEARCH_QUERY.val()))
                            .build();
                }
        );
    }

    private void batchUpdateGifs(KeyHolder keyHolder, List<Gif> gifs){
        this.jdbcTemplate.batchUpdate(
                con -> con.prepareStatement(BATCH_GIF_INSERT, Statement.RETURN_GENERATED_KEYS),
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(@NotNull PreparedStatement ps, int i) throws SQLException {
                        final Gif gif = gifs.get(i);
                        ps.setString(1, gif.getMimeType());
                        ps.setString(2, gif.getDescription());
                        ps.setString(3, gif.getTenorId());
                        ps.setString(4, gif.getTenorUrl());
                        ps.setString(5, gif.getSearchQuery());

                    }

                    @Override
                    public int getBatchSize() {
                        return gifs.size();
                    }
                },
                keyHolder
        );
    }

    private void batchUpdateFrames(List<Frame> frames){
        this.jdbcTemplate.batchUpdate(
                BATCH_FRAME_INSERT,
                frames,
                frames.size(),
                (ps, frame) -> {
                    ps.setLong(1, frame.getPHash());
                    ps.setInt(2, frame.getFrameIdx());
                    ps.setDouble(3, frame.getNmHammingDist());
                    ps.setLong(4, frame.getGif());
                }
        );
    }

}
