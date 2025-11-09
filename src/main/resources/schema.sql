CREATE TABLE gifs (
    id BIGSERIAL PRIMARY KEY,
    gif_url VARCHAR UNIQUE NOT NULL,
    tenor_id VARCHAR(50),
    name VARCHAR(50) NOT NULL,
    search_keywords VARCHAR(60),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE frame_hashes(
    id BIGSERIAL PRIMARY KEY,
    p_hash BIGINT NOT NULL ,
    gif_id BIGINT NOT NULL ,
    frame_idx INT NOT NULL ,  /* The frame of the gif this hash. This is mainly meant for gifs. Cause gifs will have more than one p_hash */
    FOREIGN KEY (gif_id) REFERENCES gifs(id) ON DELETE CASCADE
);

CREATE INDEX idx_p_hashes ON frame_hashes(p_hash);
CREATE INDEX idx_gif_id ON frame_hashes(gif_id);