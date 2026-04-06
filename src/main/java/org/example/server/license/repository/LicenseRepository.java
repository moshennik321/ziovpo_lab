package org.example.server.license.repository;

import org.example.server.license.entity.License;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
        select l
        from License l
        join DeviceLicense dl on dl.license.id = l.id
        join dl.device d
        where d.macAddress = :macAddress
          and l.user.id = :userId
          and l.product.id = :productId
          and l.blocked = false
          and l.endingDate >= :now
        order by l.endingDate desc
        """)
    List<License> findActiveLicenses(
            @Param("macAddress") String macAddress,
            @Param("userId") Long userId,
            @Param("productId") Long productId,
            @Param("now") OffsetDateTime now
    );
}