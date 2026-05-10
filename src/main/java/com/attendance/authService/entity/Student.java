package com.attendance.authService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity
@Table(name = "students")
public class Student {

    @Id
    private UUID id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(unique = true, nullable = false)
    private String studentId;

    @Column(unique = true, nullable = false)
    private String collegeRoll;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, name = "admission_year")
    private String admissionYear;

    @Column(nullable = false, name = "academic_year")
    private String academicYear;

    @Column(nullable = false)
    private String semester;

    @Temporal(TemporalType.TIMESTAMP)  //Specify date-time precision
    @Column(name="register_date", updatable = false)  //once inserted cant be update later
    private Date registerDate= new Date();
}