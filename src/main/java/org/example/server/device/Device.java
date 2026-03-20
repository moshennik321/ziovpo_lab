package org.example.server.device;

import jakarta.persistence.*;
import lombok.*;
import org.example.server.user.ApplicationUser;

@Entity
@Table(
        name = "device",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_device_mac_address", columnNames = "mac_address")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "mac_address", nullable = false, unique = true, length = 128)
    private String macAddress;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private ApplicationUser user;
}