package com.attendance.authService.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Year;
import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    @Size(max = 10)
    private String studentId;

    @Column(nullable = false)
    @Size(max = 20, min = 3)
    private String username;

    @Column(name="college_roll", unique = true)
    private String collegeRoll;

    @Column(nullable = false)
    @Size(max = 20,min=1)
    private String department;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String contact;

    @Column(nullable = false)
    private String password;

//    @ManyToOne(fetch = FetchType.EAGER)
    @Column(name = "role_id",nullable = false)
    private Long role;

    @Column(nullable = false)
    private String admission_year=String.valueOf(Year.now().getValue());

    @Temporal(TemporalType.TIMESTAMP)  //Specify date-time precision
    @Column(name="register_date", updatable = false)  //once inserted cant be update later
    private Date registerDate= new Date();
}
