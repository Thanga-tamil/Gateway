package com.mirrorfly.gateway.filter;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CustomFilter implements WebFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		ServerHttpRequest.Builder builder = setHeaders(exchange.getRequest());
		return chain.filter(exchange.mutate().request(builder.build()).build());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	private ServerHttpRequest.Builder setHeaders(ServerHttpRequest request) {
		URL url = null;
		try {
			url = request.getURI().toURL();
			String protocol = url.getProtocol();
			String host = url.getHost();
			String customDomain = protocol + "://" + host;
			log.info("customDomain ------> " + customDomain);
			ServerHttpRequest.Builder builder = request.mutate();
			builder.header(GatewayConstants.USER_DOMAIN, customDomain);
			MDC.put(GatewayConstants.USER_DOMAIN, customDomain);
			return builder;
		} catch (MalformedURLException e) {
			throw new BadDataException("Unable to identify the domain from request");
		}
	}

}
