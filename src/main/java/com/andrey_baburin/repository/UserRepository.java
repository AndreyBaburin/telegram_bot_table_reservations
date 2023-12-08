package com.andrey_baburin.repository;

import com.andrey_baburin.entity.User;
import org.springframework.data.repository.CrudRepository;
import javax.annotation.Nonnull;
import java.util.List;

public interface UserRepository extends CrudRepository<User, Long> {
    @Nonnull
    List<User> findAll();
}
