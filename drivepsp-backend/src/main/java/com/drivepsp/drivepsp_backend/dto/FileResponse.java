package com.drivepsp.drivepsp_backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record FileResponse(
        UUID id,
        String name,
        String mimeType,
        long sizeBytes,
        LocalDateTime createdAt
) {
}
