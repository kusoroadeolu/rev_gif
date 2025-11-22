package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchNormalizedGif;

public interface GifDownloadService {
    void downloadGifsFromUrl(BatchNormalizedGif result);
}
