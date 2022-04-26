package com.mirrorfly.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;

import inet.ipaddr.IPAddressString;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Slf4j
public class SecurityConfig {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http.authorizeExchange().pathMatchers("/api/v1/moderation").access(this::whiteListIp).pathMatchers("/**")
				.permitAll().and().httpBasic().and().csrf().disable();
		return http.build();
	}

	
	private Mono<AuthorizationDecision> whiteListIp(Mono<Authentication> authentication, AuthorizationContext context) {
		String ip = context.getExchange().getRequest().getRemoteAddress().getAddress().toString().replace("/", "");
		return authentication.map((a) -> new AuthorizationDecision(a.isAuthenticated()))
				.defaultIfEmpty(new AuthorizationDecision(validateIp(ip)));
	}

	private Boolean validateIp(String ip) {
		IPAddressString ipAddress = new IPAddressString(ip);
		String[] array = applicationProperties.getWhiteListIps().split(",");
		for (String rangeIp : array) {
			IPAddressString range = new IPAddressString(rangeIp);
			if (range.contains(ipAddress)) {
			return true;
			}
		}
		return false;
	}

}