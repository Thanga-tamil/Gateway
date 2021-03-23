package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.Data;

@Data
public class LicenseUpdateRequestDto {

	@NotNull(message = GatewayConstants.LICENSEKEY_EMPTY)
	@NotEmpty(message = GatewayConstants.LICENSEKEY_EMPTY)
	@NotBlank(message = GatewayConstants.LICENSEKEY_EMPTY)
	private String licenseKey;

}
