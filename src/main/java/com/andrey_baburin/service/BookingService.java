package com.andrey_baburin.service;

import com.andrey_baburin.entity.Booking;
import com.andrey_baburin.entity.SomeTable;
import com.andrey_baburin.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@RequiredArgsConstructor
@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> findById(String id) {
        if (id == null || id.isEmpty()) {
            return Optional.empty();
        }
        return bookingRepository.findById(Long.parseLong(id));
    }
    public List<Booking> findByUserId(long userId) {
        return bookingRepository.findByUserId(userId);
    }
    public List<Booking> findByDateTable(LocalDate date, SomeTable someTable) {
        return bookingRepository.findByTimeStartBetweenAndSomeTable(date.atStartOfDay(),
                date.plusDays(1).atStartOfDay(), someTable);
    }
    public void deleteById(Long id) {
        bookingRepository.deleteById(id);
    }
    public void delete(Booking booking) {
         bookingRepository.delete(booking);
    }

    public void save(Booking booking) {
        bookingRepository.save(booking);
    }
}
