package com.andrey_baburin.service;

import com.andrey_baburin.entity.User;
import com.andrey_baburin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public boolean existsById (Long id) {
        return userRepository.existsById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }


}
