package com.mirrorfly.gateway.service.impl;

import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.dto.IpDto;
import com.mirrorfly.gateway.dto.IpUpdateResponse;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.UserUnAuthorizedException;
import com.mirrorfly.gateway.repo.ChatUsersDetailsRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.IpService;
import com.mirrorfly.gateway.service.TokenService;
import com.mirrorfly.gateway.util.GatewayConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IpServiceImpl implements IpService {


    private final TokenService tokenService;

    private final ChatUsersDetailsRepo chatUsersDetailsRepo;

    private static final Set<String> ISO_COUNTRIES = Set.copyOf(Arrays.asList(Locale.getISOCountries()));
    private static final Set<String> ISO3_COUNTRIES =
            Arrays.stream(Locale.getISOCountries())
                    .map(code -> {
                        try {
                            return new Locale("", code).getISO3Country();
                        } catch (MissingResourceException e) {
                            return null; // skip invalid entries
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toUnmodifiableSet());



    @Override
    @SwitchDataSource(value="[0]")
    public Response<IpUpdateResponse> updateIp(String userdomain, IpDto ipDto, String authorization) {

        try {
            String username = tokenService.getUserNameByToken(userdomain, authorization);
            Response<IpUpdateResponse> response = new Response<>();
            if (username != null) {
                String ip = ipDto.getUserIp();
                if (!isValidIp(ip)) {
                    log.error("Invalid IP received from client: {}", ip);
                    throw new BadDataException("Invalid IP address");
                }

                if(!isValidCountryCode(ipDto.getCountryCode())){
                    log.error("Invalid country code received from client: {}", ipDto.getCountryCode());
                    throw new BadDataException("Invalid country code received from client");
                }
                int result = 0;

                        if(StringUtils.isNotBlank(ipDto.getSocketId())){
                            if(StringUtils.isNotBlank(ipDto.getDeviceId())){
                                result = chatUsersDetailsRepo.updateIpWithSocketIdAndDeviceId(ip, ipDto.getDeviceId(), username, ipDto.getCountryCode(), ipDto.getSocketId());
                                if(result != 1){
                                    return getIpUpdateResponseResponse(response, GatewayConstants.DEVICE_ID_AND_SOCKET_ID_NOT_FOUND);
                                }
                            }else {
                                result = chatUsersDetailsRepo.updateIpWithSocketId(ip, ipDto.getSocketId(), username, ipDto.getCountryCode());
                                if(result != 1){
                                    return getIpUpdateResponseResponse(response, GatewayConstants.SOCKET_ID_NOT_FOUND);
                                }
                            }
                        }else if(StringUtils.isNotBlank(ipDto.getDeviceId())) {
                            result = chatUsersDetailsRepo.updateIpWithDeviceId(ip, ipDto.getDeviceId(), username, ipDto.getCountryCode());
                            if(result != 1){
                                return getIpUpdateResponseResponse(response, GatewayConstants.DEVICE_ID_NOT_FOUND);
                            }
                        }
            }else {
                return getIpUpdateResponseResponse(response, GatewayConstants.USER_NOT_FOUND);
            }
            IpUpdateResponse ipUpdateResponse = new IpUpdateResponse();
            ipUpdateResponse.setIp(ipDto.getUserIp());
            response.setData(ipUpdateResponse);
            response.setStatus(HttpStatus.OK.value());
            response.setMessage(GatewayConstants.IP_SAVED);
            return response;

        }catch (UserUnAuthorizedException e) {
            log.error("User unauthorized", e);
            throw e;
        }
        catch (BadDataException e) {
            throw e;
        }
    }

    private Response<IpUpdateResponse> getIpUpdateResponseResponse(Response<IpUpdateResponse> response, String errorMessage) {
         response.setStatus(HttpStatus.BAD_REQUEST.value());
         response.setMessage(errorMessage);
         return response;

    }


    private boolean isValidIp(String ip) {
        if (ip == null || ip.isBlank()) {
            return false;
        }

        return InetAddressValidator.getInstance().isValid(ip.trim());
    }

    private boolean isValidCountryCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank()) {
            return false;
        }
        String code = countryCode.toUpperCase();
        return ISO_COUNTRIES.contains(code) || ISO3_COUNTRIES.contains(code);
    }
}
