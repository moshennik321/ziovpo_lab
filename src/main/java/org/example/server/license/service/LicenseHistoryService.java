package org.example.server.license.service;

import lombok.RequiredArgsConstructor;
import org.example.server.license.entity.License;
import org.example.server.license.entity.LicenseHistory;
import org.example.server.license.entity.LicenseHistoryStatus;
import org.example.server.license.repository.LicenseHistoryRepository;
import org.example.server.user.ApplicationUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LicenseHistoryService {

    private final LicenseHistoryRepository licenseHistoryRepository;

    public void saveHistory(License license, ApplicationUser actor, LicenseHistoryStatus status, String description) {
        LicenseHistory history = LicenseHistory.builder()
                .license(license)
                .user(actor)
                .status(status)
                .description(description)
                .build();

        licenseHistoryRepository.save(history);
    }
}