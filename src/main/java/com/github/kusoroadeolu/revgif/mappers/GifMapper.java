package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchTenorGif;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchNormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.NormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.TenorGif;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GifMapper {

    private final static String GIF = "gif";

    public BatchNormalizedGif gifResults(BatchTenorGif batchTenorGif, ImageClientResponse imageClientResponse){
        final List<NormalizedGif> normalizedGifs = new ArrayList<>();
        for (TenorGif result : batchTenorGif.results()){
            normalizedGifs.add(this.gifResult(result));
        }

        return new BatchNormalizedGif(normalizedGifs, imageClientResponse);
    }


    public NormalizedGif gifResult(TenorGif tenorGif){
        return new NormalizedGif(
                tenorGif.id(),
                tenorGif.mediaFormats().get(GIF).url(),
                tenorGif.contentDescription()
        );
    }

}
