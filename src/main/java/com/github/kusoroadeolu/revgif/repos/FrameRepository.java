package com.github.kusoroadeolu.revgif.repos;

import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.dtos.DbQueryResult;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Set;

public interface FrameRepository extends ListCrudRepository<Frame, Integer> {
    @Query("""
            SELECT m.tenor_url, m.name, m.content_type
            FROM frames f
                     JOIN media m ON f.media = m.id
            WHERE BIT_COUNT((f.p_hash # :userFrameHash)::BIT(64)) < :threshold
            ORDER BY BIT_COUNT((f.p_hash # :userFrameHash)::BIT(64))
            LIMIT 10;
            """)
    public Set<DbQueryResult> compareByHash(long userFrameHash, int threshold);
}
