package com.github.kusoroadeolu.revgif.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gifs")
public class Gif {

    @Id
    private Long id;
    private String mimeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description, tenorId, tenorUrl, searchQuery;
    private Set<Frame> frames;

}
