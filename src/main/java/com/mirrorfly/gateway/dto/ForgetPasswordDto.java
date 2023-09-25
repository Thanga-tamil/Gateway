package com.mirrorfly.gateway.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ForgetPasswordDto {

	@NotNull(message = "emailId cannot be Empty")
	private String emailId;
}
