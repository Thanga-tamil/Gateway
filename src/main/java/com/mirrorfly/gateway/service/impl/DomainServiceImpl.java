package com.mirrorfly.gateway.service.impl;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.DomainRequestDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.DomainService;
import com.mirrorfly.gateway.util.GatewayConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DomainServiceImpl implements DomainService {

    private final ApplicationProperties applicationProperties;

    @Override
    public Response<String> getDomain(String userdomain, DomainRequestDto domainRequestDto) {
        String domain;
        if (GatewayConstants.DEVICE_ANDROID.equalsIgnoreCase(domainRequestDto.getDeviceType())) {
            domain = applicationProperties.getHikeDomain();
            if (domain == null || domain.trim().isEmpty()) {
                throw new BadDataException("Hike domain configuration is missing");
            }
        } else if(GatewayConstants.DEVICE_IOS.equalsIgnoreCase(domainRequestDto.getDeviceType())
                 || GatewayConstants.DEVICE_WEB.equalsIgnoreCase(domainRequestDto.getDeviceType())){
            domain = applicationProperties.getTingDomain();
            if (domain == null || domain.trim().isEmpty()) {
                throw new BadDataException("Ting domain configuration is missing");
            }
        }else {
            throw new BadDataException(GatewayConstants.INVALID_DEVICE_TYPE);
        }
        Response<String> response =  new Response<>();
        response.setMessage(GatewayConstants.DOMAIN_RETRIEVED);
        response.setData(domain);
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
}
