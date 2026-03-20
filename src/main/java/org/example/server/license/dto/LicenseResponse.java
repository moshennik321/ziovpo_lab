package org.example.server.license.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record LicenseResponse(
        Long id,
        String code,
        Long productId,
        String productName,
        Long typeId,
        String typeName,
        Long ownerId,
        Long userId,
        OffsetDateTime firstActivationDate,
        OffsetDateTime endingDate,
        boolean blocked,
        Integer deviceCount,
        String description
) {}