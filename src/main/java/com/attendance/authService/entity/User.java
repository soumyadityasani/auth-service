package com.attendance.authService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    @Size(max = 25, min = 3)
    private String username;

    @Column(nullable = false)
    @Size(max = 20,min=1)
    private String department;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private String password;

    // ✅ Student mapping
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Student student;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

    @Temporal(TemporalType.TIMESTAMP)  //Specify date-time precision
    @Column(name="register_date", updatable = false)  //once inserted cant be update later
    private Date registerDate= new Date();
}
