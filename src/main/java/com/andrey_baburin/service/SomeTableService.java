package com.andrey_baburin.service;

import com.andrey_baburin.entity.SomeTable;
import com.andrey_baburin.repository.SomeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SomeTableService {

    private final SomeTableRepository someTableRepository;
    public Optional<SomeTable> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }

        return someTableRepository.findById(Long.parseLong(id));
    }

    public List<SomeTable> findAll() {
        return someTableRepository.findAll();
    }
    public void save(SomeTable someTable) {
        someTableRepository.save(someTable);
    }
}

