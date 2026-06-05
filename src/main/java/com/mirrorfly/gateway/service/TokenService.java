package com.mirrorfly.gateway.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.util.JwtKeyCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.exception.UserUnAuthorizedException;
import com.mirrorfly.gateway.model.TokenDetail;
import com.mirrorfly.gateway.repo.TokenRepo;
import com.mirrorfly.gateway.token.GenerateToken;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TokenService {

	@Autowired
	private TokenRepo tokenRepo;

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Autowired
    private JwtKeyCache jwtKeyCache;

	@Autowired
	private DataStore dataStore;

	@SwitchDataSource(value = "[0]")
	public String validateToken(String userdomain, String token) {
		log.info("requesting token: " + token);
		try {
			String u = stringRedisTemplate.opsForValue().get(token);
			if (u != null && u.contains("@")) {
				return u.substring(0, u.indexOf('@'));
			} else {
				return getTokenFromDB(userdomain, token);
			}
		} catch (Exception e) {
			log.error("Error in redis connection {}", e.getMessage());
			return getTokenFromDB(userdomain, token);
		}
	}

	public void delete(String token) {
		try {
			stringRedisTemplate.delete(token);
		} catch (Exception e) {
			log.error("Error in redis connection ", e);
		}
	}

//	@SwitchDataSource(value = "[0]")
//	public String getToken(String userdomain, String username, String devicetype, String clientIp) {
//		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
//		TokenDetail tokenDetail = new TokenDetail();
//		tokenDetail.setClientIp(clientIp);
//		tokenDetail.setCreatedAt(GatewayUtil.getCurrentDateTime());
//		tokenDetail.setDevicetype(devicetype);
//		tokenDetail.setTokenExpiryTime(GatewayUtil.getCurrentDateTime().plusHours(1l));
//		tokenDetail.setToken(GenerateToken.generateToken(tokenDetail));
//		tokenDetail.setUsername(username);
//		tokenRepo.save(tokenDetail);
//		try {
//			stringRedisTemplate.opsForValue().set(tokenDetail.getToken(), username, 1, TimeUnit.HOURS);
//		} catch (Exception e) {
//			log.error("Error in redis connection ", e.getMessage());
//			return tokenDetail.getToken();
//		}
//		return tokenDetail.getToken();
//
//	}

	/**
	 * Returns a token for the user. If the user already has a token, it simply
	 * updates the details and refreshes the expiry without generating a new one. If
	 * no token exists, it creates a new token, saves it in the database and Redis,
	 * and returns it.
	 */
    @SwitchDataSource(value = "[0]")
    public String asyncGetToken(String userdomain, String username, String deviceType, String clientIp) {

        AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		String xmppDomain = Optional.ofNullable((String) dataStore.getConnectionConfigDataByDomainAndKey(userdomain, "xmppDomain"))
				.orElseThrow(() -> new BadDataException(GatewayConstants.XMPP_DOMAIN_NOT_FOUND));

        Optional<TokenDetail> existingTokenOpt = tokenRepo.findFirstByUsername(username);

        TokenDetail tokenDetail;

		byte[] secretKey = jwtKeyCache.getSecret();
		String jid = username.concat("@").concat(xmppDomain);
		String newToken = GenerateToken.generateToken(jid, secretKey);

        if (existingTokenOpt.isPresent()) {
            tokenDetail = existingTokenOpt.get();
			String existingToken = tokenDetail.getToken();
			tokenDetail.setJid(jid); // update jid in existing records
			tokenDetail.setToken(newToken);
			tokenDetail.setClientIp(clientIp);
			tokenDetail.setDevicetype(deviceType);
			tokenDetail.setCreatedAt(GatewayUtil.getCurrentDateTime());
			tokenDetail.setTokenExpiryTime(GatewayUtil.getCurrentDateTime().plusHours(1));
            tokenRepo.save(tokenDetail);

            try {
				stringRedisTemplate.delete(existingToken);
				stringRedisTemplate.opsForValue().set(newToken, jid, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                log.error("Redis TTL update failed: {}", e.getMessage());
            }
            return tokenDetail.getToken();
        }

        tokenDetail = new TokenDetail();
		tokenDetail.setJid(jid);
		tokenDetail.setToken(newToken);
		tokenDetail.setUsername(username);
		tokenDetail.setClientIp(clientIp);
		tokenDetail.setDevicetype(deviceType);
		tokenDetail.setCreatedAt(GatewayUtil.getCurrentDateTime());
		tokenDetail.setTokenExpiryTime(GatewayUtil.getCurrentDateTime().plusHours(1));
        tokenRepo.save(tokenDetail);
        try {
            stringRedisTemplate.opsForValue().set(newToken, jid, 1, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("Redis save failed: {}", e.getMessage());
        }
        return newToken;
    }

	@SwitchDataSource(value = "[0]")
	private String getTokenFromDB(String userdomain, String header) {
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		TokenDetail token = tokenRepo.findTop1ByTokenOrderByIdDesc(header);
		if (token == null)
			return null;
		if (LocalDateTime.now().isAfter(token.getTokenExpiryTime())) {
			tokenRepo.delete(token);
			return null;
		}
		LocalDateTime tokenExtendTime = token.getTokenExpiryTime().minusMinutes(15);
		if (tokenExtendTime.isBefore(LocalDateTime.now())) {
			token.setTokenExpiryTime(LocalDateTime.now().plusMinutes(15));
			tokenRepo.save(token);
		}
		return token.getUsername();
	}

	@SwitchDataSource(value = "[0]")
	public String getUserNameByToken(String userdomain, String authorization) {
		String username = validateToken(userdomain, authorization);
		if (username == null) {
			log.error("Username not found in DB");
			throw new UserUnAuthorizedException(GatewayConstants.UNAUTHORIZED);
		}
		return username;
	}

	@SwitchDataSource(value = "[0]")
	public void deleteToken(String userdomain, String authorization) {
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		TokenDetail token = tokenRepo.findByToken(authorization);
		if (token != null) {
			try {
				stringRedisTemplate.delete(authorization);
				tokenRepo.delete(token);
			} catch (Exception e) {
				log.error("Error while removing token from Redis", e);
			}
		} else {
			log.warn("Token not found in database: {}", authorization);
		}
	}

	@SwitchDataSource(value = "[0]")
	public TokenDetail getTokenDetail(String userdomain, String token) {
		return tokenRepo.findTop1ByTokenOrderByIdDesc(token);
	}

}
