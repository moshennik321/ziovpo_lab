package org.example.server.license.service;

import lombok.RequiredArgsConstructor;
import org.example.server.common.exception.NotFoundException;
import org.example.server.license.entity.LicenseType;
import org.example.server.license.repository.LicenseTypeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LicenseTypeService {

    private final LicenseTypeRepository licenseTypeRepository;

    public LicenseType getTypeOrFail(Long typeId) {
        return licenseTypeRepository.findById(typeId)
                .orElseThrow(() -> new NotFoundException("License type not found: " + typeId));
    }
}