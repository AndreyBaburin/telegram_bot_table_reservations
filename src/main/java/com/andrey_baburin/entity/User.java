package com.andrey_baburin.entity;

import lombok.Data;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    private Long id;
    private String userName;
    private String userNumberPhone;
    private Boolean isAdmin;

    @OneToMany
    private List<Booking> bookings;
}
