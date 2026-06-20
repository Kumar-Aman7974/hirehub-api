package com.hirehub.hirehubapi.repository;

import com.hirehub.hirehubapi.enums.FileCategory;
import com.hirehub.hirehubapi.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.util.ReflectionUtils;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    Optional<FileMetadata> findByFileId(String fileId);

    List<FileMetadata> findByOwnerId(Long ownerId);

    List<FileMetadata> findByCategoryAndOwnerId(FileCategory category, Long ownerId);

    List<FileMetadata> findByRelatedEntityId(Long relatedEntityId);

    List<FileMetadata> findByCategoryAndRelatedEntityId(FileCategory category, Long relatedEntityId);

    @Query("SELECT f FROM FileMetadata f WHERE f.ownerId = :ownerId AND f.isDeleted = false ORDER BY f.createdAt DESC")
    List<FileMetadata> findActiveByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT f FROM FileMetadata f WHERE f.category = :category AND f.relatedEntityId = :entityId")
    List<FileMetadata> findByCategoryAndEntityId(@Param("category") FileCategory category,
                                                 @Param("entityId") Long entityId);

    void deleteByFileId(String fileId);
}
