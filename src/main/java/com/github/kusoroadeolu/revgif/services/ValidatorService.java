package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.wrappers.FileWrapper;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;

public interface ValidatorService {
    CompletableFuture<FileWrapper> validateFile(@NonNull MultipartFile file);
}
