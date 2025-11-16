package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.dtos.DbQueryResult;
import com.github.kusoroadeolu.revgif.repos.FrameRepository;
import com.github.kusoroadeolu.revgif.services.ImageClient;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j
@Service
public class FrameQueryServiceImpl implements com.github.kusoroadeolu.revgif.services.FrameQueryService {

    private final FrameRepository frameRepository;
    private final AppConfigProperties appConfigProperties;
    private final ImageClient client;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void findSimilarMediaFrames(HashWrapper hashWrapper){
        final Hash hash = hashWrapper.hash();
        final long hashVal = hash.getHashValue().longValue();
        final Set<DbQueryResult> res = this.frameRepository.compareByHash(hashVal,
                this.appConfigProperties.getStrictHammingThreshold());
        log.info("Results: {}", res);


        if(!res.isEmpty()){
            //this.eventPublisher.publishEvent(res); //TODO Publish event to SSE emitter
            return;
        }

        //client.getFrameDescription(hashWrapper);

    }

}
