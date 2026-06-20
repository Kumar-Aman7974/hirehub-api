package com.hirehub.hirehubapi.config;

import com.hirehub.hirehubapi.enums.FileType;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;  // ✅ ADD THIS IMPORT
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@Getter
public class FileStorageConfig {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${file.max-size:10485760}")  // 10MB
    private Long maxFileSize;

    @Value("${file.allowed-types:PDF,DOCX,JPEG,PNG}")
    private String allowedTypes;

    @Value("${file.resume-max-size:5242880}")  // 5MB for resumes
    private long resumeMaxSize;

    @Value("${file.image-max-size:2097152}")   // 2MB for images
    private long imageMaxSize;

    @PostConstruct
    public void init() {

        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }

        // Create subdirectories for each category
        String[] categories = {"resumes", "logos", "profiles", "cover-letters", "attachments"};
        for (String category : categories) {
            File categoryDir = new File(uploadDirFile, category);
            if (!categoryDir.exists()) {
                categoryDir.mkdirs();
            }
        }

        System.out.println("========================================");
        System.out.println("  📁 File Storage Configuration         ");
        System.out.println("  Upload Directory: " + uploadDirFile.getAbsolutePath());
        System.out.println("  Max File Size: " + maxFileSize / (1024 * 1024) + "MB");
        System.out.println("  Allowed Types: " + allowedTypes);
        System.out.println("========================================");
    }

    public List<FileType> getAllowedFileTypes() {
        return Arrays.stream(allowedTypes.split(","))
                .map(String::trim)
                .map(FileType::valueOf)
                .collect(Collectors.toList());
    }
}