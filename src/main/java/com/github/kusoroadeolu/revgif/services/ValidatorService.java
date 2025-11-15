package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import lombok.NonNull;
import org.springframework.web.multipart.MultipartFile;

public interface ValidatorService {
    FileWrapper validateFile(MultipartFile file);
}
