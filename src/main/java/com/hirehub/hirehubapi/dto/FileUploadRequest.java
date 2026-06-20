package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.FileCategory;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class FileUploadRequest {
    private MultipartFile file;
    private FileCategory category;
    private Long relatedEntityID; // Job ID, Application ID
}
