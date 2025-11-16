package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.DbQueryResult;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

import java.util.Set;

public interface FrameQueryService {
    Set<DbQueryResult> findGifsFromDb(HashWrapper hashWrapper);
}
