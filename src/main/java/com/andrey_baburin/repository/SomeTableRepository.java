package com.andrey_baburin.repository;

import com.andrey_baburin.entity.SomeTable;
import org.springframework.data.repository.CrudRepository;
import java.util.List;
import java.util.Optional;

public interface SomeTableRepository extends CrudRepository<SomeTable, Long> {
    Optional<SomeTable> findById(String id);

    List<SomeTable> findAll();


}
