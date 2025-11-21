package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FrameMapper {
    public Set<Frame> toFrames(List<HashWrapper> hw, double nmHmDist){
        return hw.stream()
                .map(h -> toFrame(h, nmHmDist))
                .collect(Collectors.toSet());
    }

    public Frame toFrame(HashWrapper h, double nmHmDist){
        return new Frame(null, h.hash().getHashValue().longValue(), h.frameWrapper().frameIdx(), nmHmDist ,0);
    }
}
