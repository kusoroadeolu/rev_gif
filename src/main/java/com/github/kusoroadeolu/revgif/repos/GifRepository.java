package com.github.kusoroadeolu.revgif.repos;

import com.github.kusoroadeolu.revgif.model.Gif;
import lombok.NonNull;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GifRepository extends ListCrudRepository<@NonNull Gif, @NonNull Long> {
}
