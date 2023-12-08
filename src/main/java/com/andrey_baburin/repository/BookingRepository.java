package com.andrey_baburin.repository;

import com.andrey_baburin.entity.Booking;
import com.andrey_baburin.entity.SomeTable;
import org.springframework.data.repository.CrudRepository;
import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends CrudRepository<Booking, Long> {
    @Nonnull
    List<Booking> findAll();

    List<Booking> findByTimeStartBetweenAndSomeTable(LocalDateTime fromTime, LocalDateTime toTime, SomeTable someTable);

    List<Booking> findByUserId(long userId);

    void deleteById(Long id);

    void delete(Booking booking);
}

