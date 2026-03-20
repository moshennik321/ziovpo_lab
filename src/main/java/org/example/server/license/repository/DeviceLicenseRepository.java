package org.example.server.license.repository;

import org.example.server.license.entity.DeviceLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceLicenseRepository extends JpaRepository<DeviceLicense, Long> {

    long countByLicenseId(Long licenseId);

    boolean existsByLicenseIdAndDeviceId(Long licenseId, Long deviceId);

    Optional<DeviceLicense> findByLicenseIdAndDeviceId(Long licenseId, Long deviceId);

    List<DeviceLicense> findAllByLicenseId(Long licenseId);
}