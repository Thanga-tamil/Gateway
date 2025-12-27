package com.mirrorfly.gateway.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class IpDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "User ip cannot be Null")
    @NotEmpty(message = "User ip cannot be Empty")
    private String userIp;

    @NotNull(message = "Country code cannot be Null")
    @NotEmpty(message = "Country code cannot be Empty")
    @Size(max = 10, message = "countryCode must not exceed 10 characters")
    private String countryCode;

    @NotNull(message = "Device Id code cannot be Null")
    @NotEmpty(message = "Device Id code cannot be Empty")
    private String deviceId;
}
