package com.mirrorfly.gateway.datasourcerouter;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeviceOrSocketValidator.class)
@Documented
public @interface DeviceOrSocketRequired {

    String message() default "Either deviceId or socketId must be provided";

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}