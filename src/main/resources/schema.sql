CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    content_type VARCHAR(5),
    name VARCHAR(50) NOT NULL,
    tenor_url VARCHAR UNIQUE NOT NULL,
    tenor_id VARCHAR(50) UNIQUE NOT NULL ,
    search_keywords VARCHAR(60),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE frames(
    id BIGSERIAL PRIMARY KEY,
    p_hash BIGINT NOT NULL ,
    frame_idx INT NOT NULL ,  /* The frame of the gif/image(png, jpeg, webp) this hash. This is mainly meant for gifs. Cause gifs will have more than one p_hash */
    media BIGINT NOT NULL ,
    FOREIGN KEY (media) REFERENCES media(id) ON DELETE CASCADE
);

SELECT m.tenor_url, m.name, m.content_type
FROM frames f
         JOIN media m ON f.media = m.id
WHERE BIT_COUNT(f.p_hash # :userFrameHash) < :threshold
ORDER BY BIT_COUNT(f.p_hash # :userFrameHash)
LIMIT 10;



CREATE INDEX idx_p_hashes ON frames(p_hash);
CREATE INDEX idx_media ON frames(media);


