package com.mirrorfly.gateway.controller;

import com.mirrorfly.gateway.dto.DomainRequestDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.DomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/getDomain")
@RequiredArgsConstructor
public class DomainController {

    private final DomainService domainService;

    @PostMapping
    public Response<String> getDomain(@Validated @RequestBody DomainRequestDto domainRequestDto,
                                            @RequestHeader String userdomain) {
        return domainService.getDomain(userdomain, domainRequestDto);
    }
}
