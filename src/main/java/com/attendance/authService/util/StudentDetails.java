package com.attendance.authService.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Year;

@Component
public class StudentDetails {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${college.year.break}")
    int college_year_break;



    public int calculateAdmissionYear(int semester){
        int month= LocalDate.now().getMonthValue();

        if(month<college_year_break){

            return Year.now().getValue() - ((semester+1)/2);
        }

        return (Year.now().getValue() - ((semester+1)/2) +1);
    }

    public String calculateAcademicYear(int semester){
        int month= LocalDate.now().getMonthValue();

        int current_year=Year.now().getValue();

        if(month< college_year_break){
            int previous_year= Year.now().getValue() -1;

            return String.valueOf(previous_year)+"-"+String.valueOf(current_year);
        }

        int next_year= Year.now().getValue()+1;

        return String.valueOf(current_year)+"-"+String.valueOf(next_year);
    }

    private String buildKey(String dept, String year, String sem) {
        return dept + ":" + year + ":" + sem;
    }
}
