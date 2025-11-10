package com.github.kusoroadeolu.revgif.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "frame_hashes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class FrameHash {

    @Id
    private int id;
    private long gifId;
    private long pHash;
    private int frameIdx;
    private AggregateReference<Gif, Long> gif;


}
