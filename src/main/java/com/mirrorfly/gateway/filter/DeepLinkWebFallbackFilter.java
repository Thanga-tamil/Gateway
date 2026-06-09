package com.mirrorfly.gateway.filter;

import java.net.URI;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriComponentsBuilder;

import com.mirrorfly.gateway.config.ApplicationProperties;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeepLinkWebFallbackFilter implements WebFilter, Ordered {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		String fallbackUrl = applicationProperties.getWebFallbackUrl();
		if (StringUtils.isBlank(fallbackUrl)) {
			return chain.filter(exchange);
		}

		if (!HttpMethod.GET.equals(exchange.getRequest().getMethod())) {
			return chain.filter(exchange);
		}

		String path = exchange.getRequest().getPath().value();
		if (!isDeepLinkPath(path)) {
			return chain.filter(exchange);
		}

		URI redirectUri = UriComponentsBuilder.fromUriString(fallbackUrl)
				.query(exchange.getRequest().getURI().getQuery())
				.build(true)
				.toUri();

		log.info("Redirecting deep link browser request [{}] to [{}]", path, redirectUri);
		exchange.getResponse().setStatusCode(HttpStatus.FOUND);
		exchange.getResponse().getHeaders().setLocation(redirectUri);
		return exchange.getResponse().setComplete();
	}

	private boolean isDeepLinkPath(String path) {
		return !path.startsWith("/api")
				&& !path.startsWith("/.well-known")
				&& !path.startsWith("/actuator")
				&& !path.contains("swagger")
				&& !path.contains("webjars")
				&& !path.contains("api-docs")
				&& !path.contains("swagger-resources")
				&& !path.contains("moderation")
				&& !path.contains("flyadmin");
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE + 1;
	}

}
