package org.example.server.license.repository;

import org.example.server.license.entity.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {
    List<LicenseHistory> findAllByLicenseIdOrderByChangeDateDesc(Long licenseId);
}