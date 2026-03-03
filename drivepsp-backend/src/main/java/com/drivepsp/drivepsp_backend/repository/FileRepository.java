package com.drivepsp.drivepsp_backend.repository;

import com.drivepsp.drivepsp_backend.entity.FileEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    List<FileEntity> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId);

    boolean existsByOwnerIdAndName(UUID ownerId, String name);

    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileEntity f WHERE f.owner.id = :ownerId")
    long sumarBytesPorUsuario(@Param("ownerId") UUID ownerId);

    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM FileEntity f")
    long sumarBytesTotales();
}
