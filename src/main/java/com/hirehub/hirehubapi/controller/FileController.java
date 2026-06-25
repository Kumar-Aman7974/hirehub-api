package com.hirehub.hirehubapi.controller;

import com.hirehub.hirehubapi.dto.ApiResponse;
import com.hirehub.hirehubapi.dto.FileMetadataResponse;
import com.hirehub.hirehubapi.dto.FileUploadResponse;
import com.hirehub.hirehubapi.enums.FileCategory;
import com.hirehub.hirehubapi.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "File Management", description = "APIs for file upload and download")
@SecurityRequirement(name = "Bearer Authentication")
public class FileController {

    private final FileService fileService;


    @Operation (
            summary = "Upload a file",
            description = "Upload a file to the server. Supports resumes," +
                    " profit pictures, and company logos."
    )

    @ApiResponses( value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse (responseCode = "200",
            description = "File upload successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse( responseCode = "400",
            description = "Invalid file type or size exceeded",

                        content = @Content(mediaType = "application/json",
                        examples = @ExampleObject(value = """
                                {
                                "success": false,
                                "message": "File size exceeds 5MB limit",
                                "timestamp": "2026-06-23T10:30:00"
                                }
                                
                                """))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "413",
            description = "File too large")
    })
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

    @Operation(
            summary = "Upload resume",
            description = "Uploads a resume for job applications. Only job seekers can upload."
    )
    // Upload resume for job application
    @PostMapping(value = "/upload/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('JOB_SEEKER')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadResume(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(file, FileCategory.RESUME, null);
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", response));
    }

    @Operation(
            summary = "Download file",
            description = "Downloads a file by its unique file ID."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
                    description = "File downloaded successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
                    description = "File not found")
    })
    @PostMapping(value = "/upload/profile-picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(file, FileCategory.PROFILE_PICTURE, null);
        return ResponseEntity.ok(ApiResponse.success("Profile picture uploaded successfully", response));
    }

    @Operation(
            summary = "Upload company logo",
            description = "Upload a company logo, Only EMPLOYER and ADMIN can access it"
    )
    @PostMapping(value = "/upload/company-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EMPLOYER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadCompanyLogo(
            @RequestParam("file") MultipartFile file) {

        FileUploadResponse response = fileService.uploadFile(file, FileCategory.COMPANY_LOGO, null);
        return ResponseEntity.ok(ApiResponse.success("Company logo uploaded successfully", response));
    }

    @Operation(
            summary = "Download file",
            description = "Download file by id"
    )

    @ApiResponses ( value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
            description = "File downloaded successfully"),

            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404",
            description = "File not found")
    })
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@Parameter(description = "File ID", required = true, example = "2312")
                                                     @PathVariable String fileId) {
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

    @Operation(
            summary = "Get file metadata",
            description = "get file metadata by id and Only authenticated user can access"
    )
    @GetMapping("/metadata/{fileId}")
    public ResponseEntity<ApiResponse<FileMetadataResponse>> getFileMetadata(
            @PathVariable String fileId) {
        // We need to add a method to FileService for this
        // For now, return a placeholder
        return ResponseEntity.ok(ApiResponse.success("File metadata retrieved", null));
    }

    @Operation(
            summary = "delete file data",
            description = "delete file data by fileId and Only owner can access it"
    )
    @DeleteMapping("/{fileId}")
    public ResponseEntity<ApiResponse<Void>> deleteFile(@PathVariable String fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully", null));
    }

    @Operation(
            summary = "Get all file metadata",
            description = "get all list of  file metadata"
    )
    @GetMapping("/my-files")
    public ResponseEntity<ApiResponse<List<FileMetadataResponse>>> getMyFiles() {
        // We need to add a method to FileService for this
        // For now, return an empty list
        return ResponseEntity.ok(ApiResponse.success("My files retrieved", null));
    }

    @Operation(
            summary = "Get file metadata",
            description = "get file metadata by category and Only authenticated user can access"
    )
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
