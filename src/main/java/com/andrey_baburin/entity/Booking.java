package com.andrey_baburin.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

    @Data
    @Entity
    @Table(name = "bookings")
    public class Booking {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private LocalDateTime timeStart;
        private LocalDateTime timeEnd;
        private boolean isApproved;

        @ManyToOne
        private User user;

        @ManyToOne
        private SomeTable someTable;

    }

