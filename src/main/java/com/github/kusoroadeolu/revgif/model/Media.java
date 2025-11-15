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
@Table(name = "media")
public class Media {

    @Id
    private Long id;
    private String contentType;
    private LocalDateTime createdAt;
    private String name, tenorId, tenorUrl, searchKeywords;
    private Set<Frame> frames;


    public Media setFrames(Set<Frame> frames) {
        this.frames = frames;
        return this;
    }
}
