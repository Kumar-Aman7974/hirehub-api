package com.hirehub.hirehubapi.model;

import com.hirehub.hirehubapi.enums.FileCategory;
import com.hirehub.hirehubapi.enums.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "file_metadata", indexes = {
        @Index(name = "idx_file_owner", columnList = "owner_id"),
        @Index(name = "idx_file_category", columnList = "category"),
        @Index(name = "idx_file_created_at", columnList = "created_at")
})
public class FileMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_id", nullable = false, unique = true)
    private String fileId;  // UUID

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_path", nullable = false)
    private String filePath;  // Relative path

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "file_extension")
    private String fileExtension;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private FileCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

    @Column(name = "owner_id")
    private Long ownerId;  // User or Company ID

    @Column(name = "related_entity_id")
    private Long relatedEntityId;  // Job ID, Application ID, etc.

    @Column(name = "is_public")
    private boolean isPublic = false;

    @Column(name = "download_count")
    private Integer downloadCount = 0;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Helper methods
    public void incrementDownloadCount() {
        this.downloadCount = (this.downloadCount == null ? 0 : this.downloadCount) + 1;
    }

    public String getFileUrl() {
        return "/api/files/download/" + this.fileId;
    }

    public String getFileSizeFormatted() {
        if (fileSize < 1024) return fileSize + " B";

        if (fileSize < 1024 * 1024) return String.format("%.1f KB", fileSize / 1024.0);
        if (fileSize < 1024 * 1024 * 1024) return String.format("%.1f MB", fileSize / (1024.0 * 1024.0));
        return String.format("%.1f GB", fileSize / (1024.0 * 1024.0 * 1024.0));
    }
}
