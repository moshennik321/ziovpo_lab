package org.example.server.license.service;

import lombok.RequiredArgsConstructor;
import org.example.server.common.exception.ConflictException;
import org.example.server.common.exception.ForbiddenOperationException;
import org.example.server.common.exception.NotFoundException;
import org.example.server.device.Device;
import org.example.server.device.DeviceService;
import org.example.server.license.dto.*;
import org.example.server.license.entity.*;
import org.example.server.license.repository.DeviceLicenseRepository;
import org.example.server.license.repository.LicenseRepository;
import org.example.server.product.Product;
import org.example.server.product.ProductService;
import org.example.server.user.ApplicationUser;
import org.example.server.user.ApplicationUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final ApplicationUserRepository userRepository;

    private final ProductService productService;
    private final LicenseTypeService licenseTypeService;
    private final DeviceService deviceService;
    private final LicenseCodeGenerator licenseCodeGenerator;
    private final LicenseHistoryService licenseHistoryService;
    private final TicketService ticketService;

    // ================= CREATE =================

    @Transactional
    public LicenseResponse createLicense(CreateLicenseRequest request, Long adminId) {
        Product product = productService.getProductOrFail(request.getProductId());
        productService.checkNotBlocked(product);

        LicenseType type = licenseTypeService.getTypeOrFail(request.getTypeId());

        ApplicationUser owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new NotFoundException("Owner not found"));

        ApplicationUser admin = userRepository.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Admin not found"));

        License license = License.builder()
                .code(licenseCodeGenerator.generateCode())
                .product(product)
                .type(type)
                .owner(owner)
                .blocked(false)
                .deviceCount(request.getDeviceCount())
                .description(request.getDescription())
                .build();

        License saved = licenseRepository.save(license);

        licenseHistoryService.saveHistory(
                saved,
                admin,
                LicenseHistoryStatus.CREATED,
                "License created"
        );

        return toResponse(saved);
    }

    // ================= ACTIVATE =================

    @Transactional
    public TicketResponse activateLicense(ActivateLicenseRequest request, Long userId) {

        License license = licenseRepository.findByCode(normalizeCode(request.getActivationKey()))
                .orElseThrow(() -> new NotFoundException("License not found"));

        if (license.isBlocked()) {
            throw new ConflictException("License is blocked");
        }

        productService.checkNotBlocked(license.getProduct());

        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (license.getUser() != null && !license.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("License belongs to another user");
        }

        Device device = deviceService.getOrCreateDevice(
                user,
                request.getDeviceName(),
                request.getDeviceMac()
        );

        // ===== FIRST ACTIVATION =====
        if (license.getUser() == null) {
            license.setUser(user);
            license.setFirstActivationDate(OffsetDateTime.now());
            license.setEndingDate(
                    license.getFirstActivationDate()
                            .plusDays(license.getType().getDefaultDurationInDays())
            );

            licenseRepository.save(license);

            deviceLicenseRepository.save(DeviceLicense.builder()
                    .license(license)
                    .device(device)
                    .activationDate(OffsetDateTime.now())
                    .build());

            licenseHistoryService.saveHistory(
                    license,
                    user,
                    LicenseHistoryStatus.ACTIVATED,
                    "First activation"
            );

            return ticketService.buildTicketResponse(license, device);
        }

        // ===== ALREADY ACTIVATED ON THIS DEVICE =====
        if (deviceLicenseRepository.existsByLicenseIdAndDeviceId(license.getId(), device.getId())) {
            return ticketService.buildTicketResponse(license, device);
        }

        // ===== DEVICE LIMIT CHECK =====
        long count = deviceLicenseRepository.countByLicenseId(license.getId());
        if (count >= license.getDeviceCount()) {
            throw new ConflictException("Device limit reached");
        }

        // ===== ADD NEW DEVICE =====
        deviceLicenseRepository.save(DeviceLicense.builder()
                .license(license)
                .device(device)
                .activationDate(OffsetDateTime.now())
                .build());

        licenseHistoryService.saveHistory(
                license,
                user,
                LicenseHistoryStatus.ACTIVATED,
                "Activated on new device"
        );

        return ticketService.buildTicketResponse(license, device);
    }

    // ================= CHECK =================

    @Transactional(readOnly = true)
    public TicketResponse checkLicense(CheckLicenseRequest request, Long userId) {

        Device device = deviceService.getDeviceOrFailByMac(request.getDeviceMac());

        if (!device.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("Device belongs to another user");
        }

        List<License> licenses = licenseRepository.findActiveLicenses(
                request.getDeviceMac().trim().toUpperCase(),
                userId,
                request.getProductId(),
                OffsetDateTime.now()
        );

        if (licenses.isEmpty()) {
            throw new NotFoundException("Active license not found");
        }

        License license = licenses.get(0);

        return ticketService.buildTicketResponse(license, device);
    }

    // ================= RENEW =================

    @Transactional
    public TicketResponse renewLicense(RenewLicenseRequest request, Long userId) {

        License license = licenseRepository.findByCode(normalizeCode(request.getActivationKey()))
                .orElseThrow(() -> new NotFoundException("License not found"));

        ApplicationUser user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (license.getUser() == null) {
            throw new ConflictException("License not activated");
        }

        if (!license.getUser().getId().equals(userId)) {
            throw new ForbiddenOperationException("License belongs to another user");
        }

        if (license.isBlocked()) {
            throw new ConflictException("License is blocked");
        }

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime ending = license.getEndingDate();

        boolean renewable = ending == null
                || ending.isBefore(now)
                || !ending.isAfter(now.plusDays(7));

        if (!renewable) {
            throw new ConflictException("Too early to renew");
        }

        OffsetDateTime base = (ending == null || ending.isBefore(now)) ? now : ending;

        license.setEndingDate(
                base.plusDays(license.getType().getDefaultDurationInDays())
        );

        licenseRepository.save(license);

        licenseHistoryService.saveHistory(
                license,
                user,
                LicenseHistoryStatus.RENEWED,
                "Renewed"
        );

        Device device = deviceLicenseRepository.findAllByLicenseId(license.getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ConflictException("No device"))
                .getDevice();

        return ticketService.buildTicketResponse(license, device);
    }

    // ================= UTILS =================

    private LicenseResponse toResponse(License license) {
        return LicenseResponse.builder()
                .id(license.getId())
                .code(license.getCode())
                .productId(license.getProduct().getId())
                .productName(license.getProduct().getName())
                .typeId(license.getType().getId())
                .typeName(license.getType().getName())
                .ownerId(license.getOwner().getId())
                .userId(license.getUser() != null ? license.getUser().getId() : null)
                .firstActivationDate(license.getFirstActivationDate())
                .endingDate(license.getEndingDate())
                .blocked(license.isBlocked())
                .deviceCount(license.getDeviceCount())
                .description(license.getDescription())
                .build();
    }

    private String normalizeCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Activation key must not be blank");
        }
        return code.trim().toUpperCase();
    }
}