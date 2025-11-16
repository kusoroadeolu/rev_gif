CREATE TABLE gifs (
    id BIGSERIAL PRIMARY KEY,
    mime_type VARCHAR(5),
    description VARCHAR(50) NOT NULL,
    tenor_url VARCHAR UNIQUE NOT NULL,
    tenor_id VARCHAR(50) UNIQUE NOT NULL,
    search_keywords VARCHAR(30) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE frames(
    id BIGSERIAL PRIMARY KEY,
    p_hash BIGINT NOT NULL ,
    frame_idx INT NOT NULL ,
    gifs BIGINT NOT NULL,
    FOREIGN KEY (gifs) REFERENCES gifs(id) ON DELETE CASCADE
);

CREATE INDEX idx_p_hashes ON frames(p_hash);
CREATE INDEX idx_gifs ON frames(gifs);


