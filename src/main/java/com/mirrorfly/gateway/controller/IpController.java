package com.mirrorfly.gateway.controller;

import com.mirrorfly.gateway.dto.IpDto;
import com.mirrorfly.gateway.dto.IpUpdateResponse;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.IpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class IpController {

    @Autowired
    private IpService ipService;

    @PutMapping("/updateIp")
    public Response<IpUpdateResponse> updateIp(@RequestHeader String userdomain, @Validated @RequestBody IpDto ipDto,
                                               @RequestHeader("Authorization") String authorization) {
        return ipService.updateIp(userdomain, ipDto, authorization);
    }
}
