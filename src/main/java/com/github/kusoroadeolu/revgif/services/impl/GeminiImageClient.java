package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.GeminiConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.exceptions.ImageClientException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.ImageClient;
import com.google.genai.Client;
import com.google.genai.errors.GenAiIOException;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiImageClient implements ImageClient {

    private final GeminiModelsProvider modelsProvider;
    private final GeminiConfigProperties geminiConfigProperties;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = GeminiImageClient.class.getSimpleName();
    private final static String DEFAULT_FORMAT = "png";
    private final static String DEFAULT_MIME_TYPE = "image/png";

    @Retryable(includes = ImageClientException.class)
    @Override
    public ImageClientResponse getFrameDescription(@NonNull HashWrapper wrapper) {
        try{
            final BufferedImage image = wrapper.frameWrapper().image();
            final byte[] b = this.toBytes(image);
            final Part imgPart = Part.fromBytes(b, DEFAULT_MIME_TYPE);
            final Part textPart = Part.fromText(PROMPT);
            final Tool googleSearchTool = Tool.builder()
                    .googleSearch(GoogleSearch.builder().build())
                    .build();
            final Content content = Content.fromParts(textPart, imgPart);
            final GenerateContentResponse description = this.modelsProvider.models().generateContent(
                        this.geminiConfigProperties.model(),
                        content,
                        GenerateContentConfig.builder().tools(googleSearchTool).build()
            );
            String query = description.text();

            if(query == null || query.isEmpty()){
                log.error(this.logMapper.log(CLASS_NAME, "Received null or empty response from gemini. Retrying..."));
                throw new ImageClientException();
            }

            log.info(this.logMapper.log(CLASS_NAME, "Result: %s".formatted(query)));
            return new ImageClientResponse(query, wrapper.frameWrapper().format(), wrapper.frameWrapper().frameIdx(), wrapper.hash());
        }catch (IOException e) {
            log.error(this.logMapper.log(CLASS_NAME, "An image read ex occurred."), e);
            throw new ImageClientException(e);
        }
    }

    //Helper method to convert a buf image to bytes
    private byte[] toBytes(BufferedImage image) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, DEFAULT_FORMAT, baos);
        return baos.toByteArray();
    }


    private final static String PROMPT =
            """
            Generate a 2-3 word GIF search query for this image.
            
            STEP 1: If there's a person, TRY TO IDENTIFY THEM.
            - Use Google Search if needed to identify celebrities, streamers, influencers, athletes etc.
            
            STEP 2: Identify the MOST PROMINENT visual element:
            - Is there an object they're interacting with? (phone, mic, controller, food, etc.)
            - Is a specific body part the focus? (face, hands, eyes, eyebrow)
                        
            Priority for visual anchor:
            1. Object being used/held → phone, mic, controller, car, food
            2. Body part in focus → face, hands, eyes, eyebrow, mouth
            3. Only identify the action being performed if and only if you can't identify the first two visual anchors → run, dance, scream, stare
            
            STEP 3: Build the query:
            
            1. Identified person → "[name] [object/body part/action]"
               Examples: speed mic, speed phone, rock eyebrow, khaby hands, lebron phone
               - Prefer objects over face if object is prominent
            
            2. Anime/cartoon → "[character/show] [visual]" or "anime [visual]"
               Examples: goku scream, anime blush
            
            3. Unidentifiable person → "[emotion] face"
               Examples: confused face, shocked reaction
            
            4. No person → "[subject] [action]"
               Examples: cat stare, dog side eye
            
            5. Abstract → cursed, chaotic, wtf
            
            Rules:
            - 2-3 words max
            - Object > body part > emotion (if object is prominent, use it!)
            - NO literal descriptions (pursed lips, furrowed brow)
            
            CRITICAL: Output ONLY the final keywords. No explanation. No reasoning. Just the 2-3 word query.
            
            WRONG: "The person is X, so the query is Y"
            RIGHT: speed mic
            """;

}
