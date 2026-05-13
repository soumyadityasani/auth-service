package com.attendance.authService.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class EmailWhiteListDto {
    private MultipartFile file;
}
