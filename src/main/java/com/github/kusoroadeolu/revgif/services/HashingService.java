package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class HashingService {

    private final HashingAlgorithm hasher;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = HashingService.class.getSimpleName();


    public List<HashWrapper> hashFrames(@NonNull List<FrameWrapper> frames){
            final List<HashWrapper> hws = new ArrayList<>();
            FrameWrapper fw;
            for (FrameWrapper frame : frames) {
                fw = frame;
                final Hash hash = this.hasher.hash(fw.image());
                final HashWrapper w = new HashWrapper(fw, hash);
                hws.add(w);
            }
        log.info(this.logMapper.log(CLASS_NAME, "Successfully hashed %s frames".formatted(frames.size())));
        return hws;
    }

}
