package org.example.server.license.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckLicenseRequest {

    @NotNull
    private Long productId;

    @NotBlank
    private String deviceMac;
}