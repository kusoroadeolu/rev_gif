package com.github.kusoroadeolu.revgif.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "frames")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Frame {
    @Id
    private Long id;
    private long pHash;
    private int frameIdx;
    private long gif;
}
