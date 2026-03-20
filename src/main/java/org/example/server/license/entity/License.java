package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.product.Product;
import org.example.server.user.ApplicationUser;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "license",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_license_code", columnNames = "code")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "type_id", nullable = false)
    private LicenseType type;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private ApplicationUser owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private ApplicationUser user;

    @Column(name = "first_activation_date")
    private OffsetDateTime firstActivationDate;

    @Column(name = "ending_date")
    private OffsetDateTime endingDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean blocked = false;

    @Column(name = "device_count", nullable = false)
    private Integer deviceCount;

    @Column(length = 1000)
    private String description;
}