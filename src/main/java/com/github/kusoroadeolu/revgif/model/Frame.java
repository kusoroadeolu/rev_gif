package com.github.kusoroadeolu.revgif.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "frames")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Frame {

    @Id
    private Integer id;
    private long pHash;
    private int frameIdx;

}
