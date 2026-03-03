package com.drivepsp.drivepsp_backend.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String name,
        long storageUsed,
        long storageLimit,
        long serverStorageUsed,
        long serverStorageLimit
) {
}
