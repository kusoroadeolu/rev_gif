package com.github.kusoroadeolu.revgif.repos;

import com.github.kusoroadeolu.revgif.model.Gif;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface GifRepository extends ListCrudRepository<Gif, Long> {


}
