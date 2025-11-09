package com.github.kusoroadeolu.revgif.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gifs")
public class Gif {

    @Id
    private Long id;
    private String gifUrl;
    private String tenorId;
    private String searchKeywords;
    private String name;
    private long pHash;
    private LocalDateTime createdAt;

    public Gif(String gifUrl, String tenorId, long pHash, String searchKeywords, String name){
        this.gifUrl = gifUrl;
        this.tenorId = tenorId;
        this.name = name;
        this.pHash = pHash;
        this.searchKeywords = searchKeywords;
        this.createdAt = LocalDateTime.now();
    }

    public static Gif builder(){
        return new Gif();
    }

    public Gif id(Long id) {
        this.id = id;
        return this;
    }

    public Gif gifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
        return this;
    }

    public Gif tenorId(String tenorId) {
        this.tenorId = tenorId;
        return this;
    }

    public Gif searchKeywords(String searchKeywords) {
        this.searchKeywords = searchKeywords;
        return this;
    }

    public Gif name(String name) {
        this.name = name;
        return this;
    }

    public Gif pHash(long pHash) {
        this.pHash = pHash;
        return this;
    }

}
