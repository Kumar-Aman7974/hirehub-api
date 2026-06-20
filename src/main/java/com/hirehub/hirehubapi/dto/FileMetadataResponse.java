package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.FileCategory;
import com.hirehub.hirehubapi.enums.FileType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileMetadataResponse {

    private Long id;
    private String fileId;
    private String originalFilename;
    private String fileUrl;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private FileCategory category;
    private FileType fileType;
    private Integer downloadCount;
    private LocalDateTime createdAt;
}
