package com.attendance.authService.util;

import com.attendance.authService.dto.StudentCountDto;
import com.attendance.authService.repo.StudentRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ScheduleJobs {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StudentRepo studentRepo;

    @PostConstruct
    @Scheduled(cron = "${schedule.job.time}") // daily at 3 AM
    @Transactional(readOnly = true)
    public void preloadStudentCountsScheduled() {

        try {
            System.out.println("🔄 Starting Redis student count sync...");

            redisTemplate.delete("student:count");

            List<StudentCountDto> results = studentRepo.getStudentCountsGrouped();

            for (StudentCountDto dto : results) {

                String key = dto.getDepartment() + ":" +
                        dto.getAcademicYear() + ":" +
                        dto.getSemester();

                redisTemplate.opsForHash().put("student:count", key, String.valueOf(dto.getCount()));
            }

            System.out.println("✅ Redis student count sync completed");

        } catch (Exception e) {
            System.out.println("❌ Redis sync failed: " + e.getMessage());
            e.printStackTrace(); // 🔥 ADD THIS for real debugging
        }
    }
}
