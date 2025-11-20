package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.events.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.dtos.events.GifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.repos.FrameRepository;
import com.github.kusoroadeolu.revgif.services.GifQueryService;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class GifQueryServiceImpl implements GifQueryService {

    private final FrameRepository frameRepository;
    private final AppConfigProperties appConfigProperties;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public BatchGifSearchCompletedEvent findGifsFromDb(@NonNull HashWrapper hashWrapper, String session){
        final Hash hash = hashWrapper.hash();
        final long hashVal = hash.getHashValue().longValue();
        final Set<GifSearchCompletedEvent> res = this.frameRepository.compareByHash(hashVal,
                this.appConfigProperties.nmHammingThreshold());
        final BatchGifSearchCompletedEvent bg = new BatchGifSearchCompletedEvent(res, session);

        if(!res.isEmpty()){
            this.eventPublisher.publishEvent(bg);
        }

        return bg;
    }

}
