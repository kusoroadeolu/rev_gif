package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.dtos.gif.GifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.repos.FrameRepository;
import com.github.kusoroadeolu.revgif.services.GifQueryService;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
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
                this.appConfigProperties.hammingThreshold());
        log.info("Results: {}", res);

        if(!res.isEmpty()){
            this.eventPublisher.publishEvent(res);
        }

        return new BatchGifSearchCompletedEvent(res, session);

    }

}
