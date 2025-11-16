package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchNormalizedGif;
import org.springframework.context.event.EventListener;

public interface GifDownloadService {
    void downloadGifsFromUrl(BatchNormalizedGif result);
}
