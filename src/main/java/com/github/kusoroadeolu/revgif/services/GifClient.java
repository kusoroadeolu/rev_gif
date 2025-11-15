package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;

public interface GifClient {
    void getGifs(ImageClientResponse imageClientResponse);
}
