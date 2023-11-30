package com.mirrorfly.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.Data;

@Data
public class LicenseUpdateRequestDto {

	@NotNull(message = GatewayConstants.LICENSEKEY_EMPTY)
	@NotEmpty(message = GatewayConstants.LICENSEKEY_EMPTY)
	@NotBlank(message = GatewayConstants.LICENSEKEY_EMPTY)
	private String licenseKey;

}
