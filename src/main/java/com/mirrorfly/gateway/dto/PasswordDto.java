package com.mirrorfly.gateway.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class PasswordDto {

	@NotNull(message = "NewPassword cannot be Empty")
	private String newPassword;

	@NotNull(message = "ConfirmPassword cannot be Empty")
	private String confirmPassword;

}
