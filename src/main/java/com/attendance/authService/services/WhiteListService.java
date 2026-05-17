package com.attendance.authService.services;

import com.attendance.authService.dto.ApiResponseDto;
import com.attendance.authService.dto.EmailRoleWhiteListDto;
import com.attendance.authService.dto.UploadWhiteListEmailResponseDto;
import com.attendance.authService.entity.EmailRoleWhiteList;
import com.attendance.authService.entity.WhiteListEmail;
import com.attendance.authService.repo.EmailRoleWhiteListRepo;
import com.attendance.authService.repo.WhiteListEmailRepo;
import com.attendance.authService.util.EncryptionConverter;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class WhiteListService {

    @Autowired
    private WhiteListEmailRepo whiteListRepo;

    @Autowired
    private EmailRoleWhiteListRepo emailRoleWhiteListRepo;

    @Autowired
    private EncryptionConverter encryptionConverter;

    // Upload CSV of emails
    public ResponseEntity<ApiResponseDto<UploadWhiteListEmailResponseDto>> uploadEmailsFromCSV(MultipartFile file) throws IOException {
        int added = 0, skipped = 0;

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(1); // Read first sheet

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; // skip header row

                Cell cell = row.getCell(0); // Read first column (Column A)

                if (cell == null) continue; // skip empty rows

                String email = cell.getStringCellValue().trim().toLowerCase();

                if (email.isEmpty()) continue; // ✅ skip blank cells, don't count

//                String encryptedEmail = encryptionConverter.encrypt(email);

                if (!whiteListRepo.existsByEmail(email)) {

                    WhiteListEmail whiteListEmail = new WhiteListEmail();
                    whiteListEmail.setEmail(email);

                    whiteListRepo.save(whiteListEmail);

                    added++;
                } else {
                    skipped++;
                }
            }
        }
        return  ResponseEntity.ok(new ApiResponseDto<>(
                true,
                "UPLOAD SUCCES S",
                new UploadWhiteListEmailResponseDto(
                        added,
                        skipped
                ),
                LocalDateTime.now()
        ));
    }

    // Check if email is whitelisted and unused
    public boolean isEligible(String email) {

        return whiteListRepo.findByEmail(email)
                .map(e -> !e.isUsed())
                .orElse(false);
    }

    // Mark email as used after successful registration
    public void markAsUsed(String email) {

        whiteListRepo.findByEmail(email)
                .ifPresent(e -> {
                    e.setUsed(true);
                    whiteListRepo.save(e);
                });
    }

    public ResponseEntity<ApiResponseDto<?>> addEmailRoleFaculty(EmailRoleWhiteListDto request) {

        if (emailRoleWhiteListRepo.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponseDto<>(false,
                            "EMAIL ALREADY IN WHITELIST",
                            null,
                            LocalDateTime.now()));
        }

        EmailRoleWhiteList entry = new EmailRoleWhiteList();
        entry.setEmail(request.getEmail());
        entry.setAssignedRole(request.getAssignedRole()); // "ROLE_FACULTY", "ROLE_HOD"
        entry.setUsed(false);

        emailRoleWhiteListRepo.save(entry);

        return  ResponseEntity.ok(new ApiResponseDto<>(
                true,
                "UPLOAD SUCCESS",
                null,
                LocalDateTime.now()
        ));
    }
}
