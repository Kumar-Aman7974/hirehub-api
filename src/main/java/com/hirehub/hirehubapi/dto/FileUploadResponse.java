package com.hirehub.hirehubapi.dto;

import com.hirehub.hirehubapi.enums.FileCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileUploadResponse {

    private String fileId;
    private String originalFilename;
    private String storedFilename;
    private String fileUrl;
    private Long fileSize;
    private String fileSizeFormatted;
    private String mimeType;
    private FileCategory category;
    private String downloadUrl;
    private String publicUrl;

}
