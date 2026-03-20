package org.example.server.device;

import lombok.RequiredArgsConstructor;
import org.example.server.common.exception.ConflictException;
import org.example.server.common.exception.NotFoundException;
import org.example.server.user.ApplicationUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public Device getDeviceOrFailByMac(String macAddress) {
        return deviceRepository.findByMacAddress(normalizeMac(macAddress))
                .orElseThrow(() -> new NotFoundException("Device not found"));
    }

    public Device getOrCreateDevice(ApplicationUser user, String deviceName, String macAddress) {
        String normalizedMac = normalizeMac(macAddress);

        return deviceRepository.findByMacAddress(normalizedMac)
                .map(existing -> {
                    if (!existing.getUser().getId().equals(user.getId())) {
                        throw new ConflictException("Device belongs to another user");
                    }
                    if (deviceName != null && !deviceName.isBlank()) {
                        existing.setName(deviceName.trim());
                        return deviceRepository.save(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> deviceRepository.save(
                        Device.builder()
                                .name((deviceName == null || deviceName.isBlank()) ? "Unknown device" : deviceName.trim())
                                .macAddress(normalizedMac)
                                .user(user)
                                .build()
                ));
    }

    private String normalizeMac(String macAddress) {
        if (macAddress == null || macAddress.isBlank()) {
            throw new IllegalArgumentException("Device MAC must not be blank");
        }
        return macAddress.trim().toUpperCase();
    }
}