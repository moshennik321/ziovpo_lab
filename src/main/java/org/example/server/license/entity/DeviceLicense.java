package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.device.Device;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "device_license",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_device_license_license_device",
                        columnNames = {"license_id", "device_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceLicense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Builder.Default
    @Column(name = "activation_date", nullable = false)
    private OffsetDateTime activationDate = OffsetDateTime.now();
}