package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FrameMapper {
    public Set<Frame> toFrame(List<HashWrapper> hw){
        return hw.stream()
                .map(h -> new Frame(null, h.hash().getHashValue().longValueExact(), h.frameWrapper().frameIdx(), 0))
                .collect(Collectors.toSet());
    }
}
