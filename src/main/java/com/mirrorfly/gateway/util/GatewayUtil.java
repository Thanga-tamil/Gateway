package com.mirrorfly.gateway.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.exception.BadDataException;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GatewayUtil {

    @Autowired
    private ApplicationProperties applicationProperties;

    private GatewayUtil() {
    }

    private static SecureRandom rnd = new SecureRandom();

    public static PageRequest getPagination(Integer page, Integer size) {
        page = page != null ? page : 0;
        size = size != null ? size : 10;
        return PageRequest.of(page, size);
    }

    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }

    public static String getName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    // function to generate a random string of length n
    public static String getAlphaNumericString(int n) {
        String alphaNumericString = "0123456789" + "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + System.currentTimeMillis()
                + "abcdefghijklmnopqrstuvxyz" + "0123456789";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(alphaNumericString.charAt(rnd.nextInt(alphaNumericString.length())));
        }
        return sb.toString();
    }

	public static String getMd5String(String input) {
		StringBuilder hashtext = new StringBuilder();
		try {

			MessageDigest md = MessageDigest.getInstance("MD5"); //NOSONAR not used in secure contexts

			byte[] messageDigest = md.digest(input.getBytes());
			BigInteger no = new BigInteger(1, messageDigest);
			hashtext.append(no.toString(16));
			while (hashtext.length() < 32) {
				hashtext.append("0" + hashtext);
			}
		} catch (NoSuchAlgorithmException e) {
			log.error("Error while create string to MD5");
			log.error(e.getMessage());
		}
		return hashtext.toString();
	}

    public static Map<String, Object> convertJsonStringToMap(String jsonString) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonString, new TypeReference<Map<String, Object>>() {
        });
    }

    public static String convertMapToJson(Map<String, Object> messageContent) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(messageContent);
    }

    public static String convertMetaDataAsJson(Map<String, Object> metaData) {
        String jsonMetadata = null;
        if (metaData != null && metaData.size() > 0) {
            try {
                Map<String, Object> meta = new HashMap<>();
                for (Map.Entry<String, Object> entry : metaData.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        meta.put(entry.getKey().trim(), entry.getValue().toString().trim());
                    } else {
                        meta.put(entry.getKey().trim(), entry.getValue());
                    }
                }
                jsonMetadata = convertMapToJson(meta);
            } catch (JsonProcessingException e) {
                throw new BadDataException("Json Parsing Error");
            }
        }
        return jsonMetadata;
    }

    public static void validateMetaData(Map<String, Object> meta, int maxLimit) {
        if (meta != null && meta.size() > 0) {
            if (meta.size() > maxLimit) {
                throw new BadDataException("Metadata can have maximum of " + maxLimit + " fields");
            } else {
                for (Map.Entry<String, Object> entry : meta.entrySet()) {
                    if (entry.getKey() == null || entry.getKey().trim().length() == 0) {
                        throw new BadDataException("Metadata key can not be empty");
                    } else if (entry.getValue() == null || entry.getValue().toString().trim().length() == 0) {
                        throw new BadDataException(
                                "Metadata value for the key " + entry.getKey().trim() + " is required");
                    }
                }
                // just to verify if given data can safely converted to json string
                convertMetaDataAsJson(meta);
            }
        }
    }

    public static void validateUserType(String userType, List<String> userTypes) {
        if (StringUtils.isNotBlank(userType) && !userTypes.contains(userType.trim().toLowerCase())) {
            throw new BadDataException(GatewayConstants.USER_TYPE_NOT_VALID);
        }
    }

	public Boolean userIpAccessValidation(String remoteIP) {
		if (StringUtils.isBlank(remoteIP)) {
			log.error("userIpAccessValidation - Remote IP is null or Blank " + remoteIP);
			return false;
		}

		try {
			log.debug("userIpAccessValidation - Ip address received " + remoteIP);

			String startRange = applicationProperties.getWhiteListIpStartRange();
			String endRange = applicationProperties.getWhiteListIpEndRange();
			if (StringUtils.isBlank(startRange) || StringUtils.isBlank(endRange)) {
				log.error("userIpAccessValidation - WhiteListIP ranges missing startRange " + startRange + "  endRange "
						+ endRange);
				return false;
			}

			log.debug(
					"userIpAccessValidation - Allowed Ip ranges  startRange " + startRange + "  endRange " + endRange);

			IPAddress startIPAddress = new IPAddressString(startRange).getAddress();
			IPAddress endIPAddress = new IPAddressString(endRange).getAddress();
			IPAddressSeqRange ipRange = startIPAddress.spanWithRange(endIPAddress);
			IPAddress inputIPAddress = new IPAddressString(remoteIP).toAddress();
			
			if (ipRange.contains(inputIPAddress)) {
				log.debug("userIpAccessValidation - ip address validation successs, hence allowing ip adresss ------> "
						+ remoteIP);
				return true;
			} else {
				log.error(
						"userIpAccessValidation - ip address validation failed, hence request will be dropped,  startRange ------> "
								+ startRange + "  endRange ------> " + endRange + "  incoming ip adresss ------> "
								+ remoteIP);
				return false;
			}

		} catch (Exception e) {
			log.error("userIpAccessValidation - Error in verifying Ip white listing " + e.getMessage());
			return false;
		}
	}
    
}
