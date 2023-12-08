package com.andrey_baburin.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "tables")
public class SomeTable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private LocalTime availableFrom;
    private LocalTime availableTo;

}
