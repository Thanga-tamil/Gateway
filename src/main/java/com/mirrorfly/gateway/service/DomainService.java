package com.mirrorfly.gateway.service;

import com.mirrorfly.gateway.dto.DomainRequestDto;
import com.mirrorfly.gateway.response.Response;

public interface DomainService {
    Response<String> getDomain(String userdomain, DomainRequestDto domainRequestDto);
}
