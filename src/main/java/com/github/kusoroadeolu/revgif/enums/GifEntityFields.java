package com.github.kusoroadeolu.revgif.enums;

public enum GifEntityFields {
    ID("id"),
    MIME_TYPE("mime_type"),
    DESCRIPTION("description"),
    TENOR_URL("tenor_url"),
    TENOR_ID("tenor_id"),
    SEARCH_QUERY("search_query");

    private final String val;

    GifEntityFields(String val){
        this.val = val;
    }

    public String val(){
        return this.val;
    }
}
