package org.example.server.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActivateLicenseRequest {

    @NotBlank
    private String activationKey;

    @NotBlank
    private String deviceName;

    @NotBlank
    private String deviceMac;
}