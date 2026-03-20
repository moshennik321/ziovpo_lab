package org.example.server.license.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLicenseRequest {

    @NotNull
    private Long productId;

    @NotNull
    private Long typeId;

    @NotNull
    private Long ownerId;

    @NotNull
    @Min(1)
    private Integer deviceCount;

    private String description;
}