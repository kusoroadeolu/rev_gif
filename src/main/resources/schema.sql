CREATE TABLE gifs (
    id BIGSERIAL PRIMARY KEY,
    mime_type VARCHAR(20),
    description VARCHAR(300) NOT NULL,
    tenor_url VARCHAR(300) NOT NULL,
    tenor_id VARCHAR(200) UNIQUE NOT NULL,
    search_query VARCHAR(30) NOT NULL,
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

-- ALTER TABLE gifs ALTER COLUMN description TYPE VARCHAR(300);
-- ALTER TABLE gifs ALTER COLUMN tenor_url TYPE VARCHAR(300);
-- ALTER TABLE gifs ALTER COLUMN tenor_id TYPE VARCHAR (300);

CREATE INDEX idx_p_hashes ON frames(p_hash);
CREATE INDEX idx_gifs ON frames(gifs);


