package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.SseWrapper;

public interface SseService {
    void emit(String session, Object val);

    void remove(String session);

    SseWrapper getWrapper(String session);
}
