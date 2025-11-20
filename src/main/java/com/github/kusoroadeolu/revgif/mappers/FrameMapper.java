package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FrameMapper {
    public Set<Frame> toFrames(List<HashWrapper> hw){
        return hw.stream()
                .map(this::toFrame)
                .collect(Collectors.toSet());
    }

    public Frame toFrame(HashWrapper h){
        return new Frame(null, h.hash().getHashValue().longValue(), h.frameWrapper().frameIdx(), 0);
    }
}
