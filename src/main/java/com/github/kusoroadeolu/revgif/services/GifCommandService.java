package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.Gif;

import java.util.List;

public interface GifCommandService {
    void batchSave(List<Gif> gifs);
}
