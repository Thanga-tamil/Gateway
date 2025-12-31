package com.mirrorfly.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DomainRequestDto {

    @NotBlank(message = "Device type must not be null or empty")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Device type must contain letters only"
    )
    private String deviceType;
}
