package com.mirrorfly.gateway.service;

import com.mirrorfly.gateway.dto.IpDto;
import com.mirrorfly.gateway.dto.IpUpdateResponse;
import com.mirrorfly.gateway.response.Response;

public interface IpService {
    Response<IpUpdateResponse> updateIp(String userdomain, IpDto ipDto, String authorization);
}
