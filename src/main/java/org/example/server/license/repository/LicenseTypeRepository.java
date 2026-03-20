package org.example.server.license.repository;

import org.example.server.license.entity.LicenseType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LicenseTypeRepository extends JpaRepository<LicenseType, Long> {
    Optional<LicenseType> findByNameIgnoreCase(String name);
}