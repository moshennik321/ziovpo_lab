package org.example.server.license.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record Ticket(
        OffsetDateTime serverDate,
        Long ttlSeconds,
        OffsetDateTime activationDate,
        OffsetDateTime expirationDate,
        Long userId,
        Long deviceId,
        boolean blocked
) {}