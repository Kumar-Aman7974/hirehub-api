package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.FileMetadataResponse;
import com.hirehub.hirehubapi.dto.FileUploadResponse;
import com.hirehub.hirehubapi.enums.FileCategory;
import com.hirehub.hirehubapi.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    //Upload a file, Access: Authentication users
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") FileCategory category,
            @RequestParam(value = "relatedEntityId", required = false) Long relatedEntityId) {

        log.info("File upload request: category={}, size={}, name={}",
                category, file.getSize(), file.getOriginalFilename());

        FileUploadResponse response = fileService.uploadFile(file, category, relatedEntityId);
        return ResponseEntity.ok(ApiResponse.success("File uploaded successfully", response));
    }

    // Upload resume for job application
    @PostMapping(value = "/upload/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadResume(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(file, FileCategory.RESUME, null);
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", response));
    }

    /**
     * Upload profile picture
     * POST /api/files/upload/profile-picture
     * Access: Authenticated users
     */
    @PostMapping(value = "/upload/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(file, FileCategory.PROFILE_PICTURE, null);
        return ResponseEntity.ok(ApiResponse.success("Profile picture uploaded successfully", response));
    }

    /**
     * Upload company logo
     * POST /api/files/upload/company-logo
     * Access: EMPLOYER, ADMIN
     */
    @PostMapping(value = "/upload/company-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadCompanyLogo(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(file, FileCategory.COMPANY_LOGO, null);
        return ResponseEntity.ok(ApiResponse.success("Company logo uploaded successfully", response));
    }

    /**
     * Download file by file ID
     * GET /api/files/download/{fileId}
     * Access: Authenticated users (with permission)
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileId) {
        Resource resource = fileService.downloadFile(fileId);

        // Get content type from file name or default
        String contentType;
        try {
            contentType = resource.getFile().toPath().toString().endsWith(".pdf") ?
                    "application/pdf" : "application/octet-stream";
        } catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileId + "\"")
                .body(resource);
    }

    /**
     * Get file metadata
     * GET /api/files/metadata/{fileId}
     * Access: Authenticated users
     */
    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> getFileMetadata(
            @PathVariable String fileId) {
        // We need to add a method to FileService for this
        // For now, return a placeholder
        return ResponseEntity.ok(ApiResponse.success("File metadata retrieved", null));
    }

    /**
     * Delete file
     * DELETE /api/files/{fileId}
     * Access: Authenticated users (owner)
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    /**
     * Get my files (all categories)
     * GET /api/files/my-files
     * Access: Authenticated users
     */
    @GetMapping("/my-files")
    public ResponseEntity<ApiResponse<List<FileMetadataResponse>>> getMyFiles() {
        // We need to add a method to FileService for this
        // For now, return an empty list
        return ResponseEntity.ok(ApiResponse.success("My files retrieved", null));
    }

    /**
     * Get files by category
     * GET /api/files/category/{category}
     * Access: Authenticated users
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<FileMetadataResponse>>> getFilesByCategory(
            @PathVariable FileCategory category) {
        List<FileMetadataResponse> files = fileService.getFilesByCategory(category);
        return ResponseEntity.ok(ApiResponse.success("Files retrieved successfully", files));
    }
}
