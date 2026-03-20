package org.example.server.license.dto;

import lombok.Builder;

@Builder
public record TicketResponse(
        Ticket ticket,
        String signature
) {}