package com.mirrorfly.gateway.datasourcerouter;

import com.mirrorfly.gateway.dto.IpDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DeviceOrSocketValidator
        implements ConstraintValidator<DeviceOrSocketRequired, IpDto> {

    @Override
    public boolean isValid(IpDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        boolean hasDeviceId =
                dto.getDeviceId() != null && !dto.getDeviceId().trim().isEmpty();

        boolean hasSocketId =
                dto.getSocketId() != null && !dto.getSocketId().trim().isEmpty();

        if (hasDeviceId || hasSocketId) {
            return true;
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                        "Either deviceId or socketId must be provided")
                .addPropertyNode("deviceId")
                .addConstraintViolation();

        return false;
    }
}
