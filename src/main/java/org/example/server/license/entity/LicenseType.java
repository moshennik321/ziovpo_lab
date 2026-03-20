package org.example.server.license.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "license_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LicenseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "default_duration_in_days", nullable = false)
    private Integer defaultDurationInDays;

    @Column(length = 1000)
    private String description;
}