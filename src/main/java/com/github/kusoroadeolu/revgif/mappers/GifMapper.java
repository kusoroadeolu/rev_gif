package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchTenorGifResult;
import com.github.kusoroadeolu.revgif.dtos.gif.GifResult;
import com.github.kusoroadeolu.revgif.dtos.gif.NormalizedGifResult;
import com.github.kusoroadeolu.revgif.dtos.gif.TenorGifResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GifMapper {

    private final static String GIF = "gif";

    public GifResult gifResults(BatchTenorGifResult batchTenorGifResult, ImageClientResponse imageClientResponse){
        final List<NormalizedGifResult> normalizedGifResults = new ArrayList<>();
        for (TenorGifResult result : batchTenorGifResult.results()){
            normalizedGifResults.add(this.gifResult(result));
        }

        return new GifResult(normalizedGifResults, imageClientResponse);
    }


    public NormalizedGifResult gifResult(TenorGifResult tenorGifResult){
        return new NormalizedGifResult(
                tenorGifResult.id(),
                tenorGifResult.mediaFormats().get(GIF).url(),
                tenorGifResult.title()
        );
    }

}
