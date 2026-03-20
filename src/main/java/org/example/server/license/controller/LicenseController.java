package org.example.server.license.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.server.common.security.SecurityUtils;
import org.example.server.license.dto.*;
import org.example.server.license.service.LicenseService;
import org.example.server.user.ApplicationUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;
    private final ApplicationUserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseResponse> createLicense(@Valid @RequestBody CreateLicenseRequest request) {
        Long adminId = getCurrentUserId();
        LicenseResponse response = licenseService.createLicense(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activateLicense(@Valid @RequestBody ActivateLicenseRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(licenseService.activateLicense(request, userId));
    }

    @PostMapping("/check")
    public ResponseEntity<TicketResponse> checkLicense(@Valid @RequestBody CheckLicenseRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(licenseService.checkLicense(request, userId));
    }

    @PostMapping("/renew")
    public ResponseEntity<TicketResponse> renewLicense(@Valid @RequestBody RenewLicenseRequest request) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(licenseService.renewLicense(request, userId));
    }

    private Long getCurrentUserId() {
        String email = SecurityUtils.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"))
                .getId();
    }
}