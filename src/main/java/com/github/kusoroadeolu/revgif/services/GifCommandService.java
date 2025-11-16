package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.Gif;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GifCommandService {
    @Transactional
    void batchSave(List<Gif> gifs);
}
