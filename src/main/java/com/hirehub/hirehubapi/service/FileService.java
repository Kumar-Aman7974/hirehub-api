package com.hirehub.hirehubapi.service;

import com.hirehub.hirehubapi.config.FileStorageConfig;
import com.hirehub.hirehubapi.dto.FileMetadataResponse;
import com.hirehub.hirehubapi.dto.FileUploadRequest;
import com.hirehub.hirehubapi.dto.FileUploadResponse;
import com.hirehub.hirehubapi.enums.FileCategory;
import com.hirehub.hirehubapi.enums.FileType;
import com.hirehub.hirehubapi.exception.ResourceNotFoundException;
import com.hirehub.hirehubapi.model.FileMetadata;
import com.hirehub.hirehubapi.model.User;
import com.hirehub.hirehubapi.repository.FileMetadataRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileStorageConfig fileStorageConfig;
    private final FileMetadataRepository fileMetadataRepository;
    private final UserService userService;

    /**
     * Upload a file
     */
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, FileCategory category,
                                         Long relatedEntityId) {
        // 1. Validate user is authenticated
        User currentUser = userService.getCurrentUser();

        // 2. Validate file
        validateFile(file, category);

        // 3. Generate secure filename
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String storedFilename = generateSecureFilename(originalFilename);

        // 4. Create directory structure
        String subPath = buildStoragePath(category, currentUser.getId());
        Path uploadPath = Paths.get(fileStorageConfig.getUploadDir(), subPath);

        try{
            //5. Create directories
            Files.createDirectories(uploadPath);

            // 6. Save file
            Path filePath = uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 7. Save file metadata
            String fileId = UUID.randomUUID().toString();
            FileMetadata metadata = FileMetadata.builder()
                    .fileId(fileId)
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .filePath(subPath + "/" + storedFilename)
                    .fileSize(file.getSize())
                    .mimeType(file.getContentType())
                    .fileExtension(extension)
                    .category(category)
                    .fileType(FileType.fromMimeType(file.getContentType()))
                    .ownerId(currentUser.getId())
                    .relatedEntityId(relatedEntityId)
                    .build();

            FileMetadata savedMetadata = fileMetadataRepository.save(metadata);

            // 8. Build response
            return FileUploadResponse.builder()
                    .fileId(savedMetadata.getFileId())
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .fileUrl(savedMetadata.getFileUrl())
                    .fileSize(file.getSize())
                    .fileSizeFormatted(savedMetadata.getFileSizeFormatted())
                    .mimeType(file.getContentType())
                    .category(category)
                    .downloadUrl("/api/files/download/" + fileId)
                    .build();

        } catch (IOException e) {
            log.error("Failed to save file: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file: " + e.getMessage());

        }

    }

    /**
     * Download file by file ID
     */
    public Resource downloadFile(String fileId) {
        // 1. Get metadata
        FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        // 2. Check access
        User currentUser = userService.getCurrentUser();
        checkFileAccess(metadata, currentUser);

        // 3. Load file
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir(), metadata.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                throw new ResourceNotFoundException("File not found on disk");
            }

            // Increment download count
            metadata.incrementDownloadCount();
            fileMetadataRepository.save(metadata);

            return resource;

        } catch (MalformedURLException e) {
            log.error("Error reading file: {}", e.getMessage());
            throw new RuntimeException("Failed to read file: " + e.getMessage());
        }
    }

    /**
     * Delete file
     */
    @Transactional
    public void deleteFile(String fileId) {
        // 1. Get metadata
        FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        // 2. Check access
        User currentUser = userService.getCurrentUser();
        checkFileAccess(metadata, currentUser);

        // 3. Delete physical file
        try {
            Path filePath = Paths.get(fileStorageConfig.getUploadDir(), metadata.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("File deleted from disk: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to delete file from disk: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file: " + e.getMessage());
        }

        // 4. Delete metadata
        fileMetadataRepository.delete(metadata);
        log.info("File metadata deleted: {}", fileId);
    }

    /**
     * Get files by owner
     */
    public List<FileMetadataResponse> getFilesByOwner(Long ownerId) {
        List<FileMetadata> files = fileMetadataRepository.findByOwnerId(ownerId);
        return files.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get files by category
     */
    public List<FileMetadataResponse> getFilesByCategory(FileCategory category) {
        User currentUser = userService.getCurrentUser();
        List<FileMetadata> files = fileMetadataRepository.findByCategoryAndOwnerId(category, currentUser.getId());
        return files.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ========== VALIDATION METHODS ==========

    private void validateFile(MultipartFile file, FileCategory category) {
        // 1. Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // 2. Check file size
        long maxSize = getMaxSizeForCategory(category);
        if (file.getSize() > maxSize) {
            String sizeMB = maxSize / (1024 * 1024) + "MB";
            throw new IllegalArgumentException("File size exceeds " + sizeMB + " limit");
        }

        // 3. Check file type
        String contentType = file.getContentType();
        FileType fileType = FileType.fromMimeType(contentType);

        if (fileType == null) {
            throw new IllegalArgumentException("File type not supported: " + contentType);
        }

        // 4. Check extension matches MIME type
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        boolean extensionValid = false;
        for (String ext : fileType.getExtensions()) {
            if (ext.equalsIgnoreCase(extension)) {
                extensionValid = true;
                break;
            }
        }

        if (!extensionValid) {
            throw new IllegalArgumentException("File extension does not match MIME type");
        }

        // 5. Check if file type is allowed
        if (!fileStorageConfig.getAllowedFileTypes().contains(fileType)) {
            throw new IllegalArgumentException("File type not allowed: " + fileType);
        }

        // 6. Category-specific validation
        validateCategorySpecific(fileType, category);
    }

    private void validateCategorySpecific(FileType fileType, FileCategory category) {
        switch (category) {
            case RESUME:
                if (!fileType.isDocument()) {
                    throw new IllegalArgumentException("Resume must be a document (PDF, DOCX, etc.)");
                }
                break;
            case COMPANY_LOGO:
            case PROFILE_PICTURE:
                if (!fileType.isImage()) {
                    throw new IllegalArgumentException("Profile image must be an image file");
                }
                break;
            default:
                // No specific validation
        }
    }

    private long getMaxSizeForCategory(FileCategory category) {
        switch (category) {
            case RESUME:
                return fileStorageConfig.getResumeMaxSize();
            case COMPANY_LOGO:
            case PROFILE_PICTURE:
                return fileStorageConfig.getImageMaxSize();
            default:
                return fileStorageConfig.getMaxFileSize();
        }
    }

    // ========== HELPER METHODS ==========

    private String generateSecureFilename(String originalFilename) {
        String extension = FilenameUtils.getExtension(originalFilename);
        String baseName = FilenameUtils.getBaseName(originalFilename);

        // Sanitize: remove special characters
        String sanitized = baseName.replaceAll("[^a-zA-Z0-9]", "_");

        // Add timestamp and UUID to prevent collisions
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        return sanitized + "_" + timestamp + "_" + uuid + "." + extension;
    }

    private String buildStoragePath(FileCategory category, Long userId) {
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        return category.getDirectory() + "/" + dateDir + "/" + userId;
    }

    private void checkFileAccess(FileMetadata metadata, User currentUser) {
        // Admin has access to all files
        if (currentUser.getRole().name().equals("ADMIN")) {
            return;
        }

        // Check if user is the owner
        if (metadata.getOwnerId().equals(currentUser.getId())) {
            return;
        }

        // Employers can see resumes of applicants
        // This will be handled at the controller level with job/application checks
        // For now, only owner can access

        throw new AccessDeniedException("You don't have permission to access this file");
    }

    private FileMetadataResponse mapToResponse(FileMetadata metadata) {
        return FileMetadataResponse.builder()
                .id(metadata.getId())
                .fileId(metadata.getFileId())
                .originalFilename(metadata.getOriginalFilename())
                .fileUrl(metadata.getFileUrl())
                .fileSize(metadata.getFileSize())
                .fileSizeFormatted(metadata.getFileSizeFormatted())
                .mimeType(metadata.getMimeType())
                .category(metadata.getCategory())
                .fileType(metadata.getFileType())
                .downloadCount(metadata.getDownloadCount())
                .createdAt(metadata.getCreatedAt())
                .build();
    }
}
