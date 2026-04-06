package org.example.server.license.service;

import lombok.RequiredArgsConstructor;
import org.example.server.device.Device;
import org.example.server.license.dto.Ticket;
import org.example.server.license.dto.TicketResponse;
import org.example.server.license.entity.License;
import org.example.server.signature.SigningService;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final long DEFAULT_TTL_SECONDS = 300L;

    private final SigningService signingService;

    public Ticket buildTicket(License license, Device device) {
        return Ticket.builder()
                .serverDate(OffsetDateTime.now())
                .ttlSeconds(DEFAULT_TTL_SECONDS)
                .activationDate(license.getFirstActivationDate())
                .expirationDate(license.getEndingDate())
                .userId(license.getUser() != null ? license.getUser().getId() : null)
                .deviceId(device.getId())
                .blocked(license.isBlocked())
                .build();
    }

    public TicketResponse buildTicketResponse(License license, Device device) {
        Ticket ticket = buildTicket(license, device);
        String digitalSignature = signingService.sign(ticket);

        return TicketResponse.builder()
                .ticket(ticket)
                .signature(digitalSignature)
                .build();
    }
}