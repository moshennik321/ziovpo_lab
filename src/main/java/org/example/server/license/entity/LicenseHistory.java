package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.user.ApplicationUser;

import java.time.OffsetDateTime;

@Entity
@Table(name = "license_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LicenseHistoryStatus status;

    @Builder.Default
    @Column(name = "change_date", nullable = false)
    private OffsetDateTime changeDate = OffsetDateTime.now();

    @Column(length = 1000)
    private String description;
}