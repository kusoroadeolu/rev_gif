package com.github.kusoroadeolu.revgif.services.impl;

import com.google.genai.Client;
import com.google.genai.Models;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GeminiModelsProvider {
    private final Client geminiClient;

    public Models models(){
        return this.geminiClient.models;
    }
}
