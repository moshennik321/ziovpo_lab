package org.example.server.license.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RenewLicenseRequest {

    @NotBlank
    private String activationKey;
}