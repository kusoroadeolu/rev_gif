package com.github.kusoroadeolu.revgif.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "frame_hashes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class FrameHash {

    @Id
    private int id;

    private int gifId;

    private long pHash;

    private int frameIdx;

    private static FrameHash builder(){
        return new FrameHash();
    }

    public FrameHash id(int id) {
        this.id = id;
        return this;
    }

    public FrameHash gifId(int gifId) {
        this.gifId = gifId;
        return this;
    }

    public FrameHash pHash(long pHash) {
        this.pHash = pHash;
        return this;
    }

    public FrameHash frameIdx(int frameIdx) {
        this.frameIdx = frameIdx;
        return this;
    }
}
