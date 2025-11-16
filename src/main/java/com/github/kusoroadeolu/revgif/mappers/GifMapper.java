package com.github.kusoroadeolu.revgif.mappers;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.gif.*;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Gif toGif(HashedGif gif, String query, String format, Set<Frame> frames){
        final NormalizedGif nm = gif.normalizedGif();
        return Gif
                .builder()
                .mimeType("image/"+format)
                .tenorId(nm.id())
                .tenorUrl(nm.url())
                .searchQuery(query)
                .description(nm.description())
                .frames(frames)
                .build();
    }



}
