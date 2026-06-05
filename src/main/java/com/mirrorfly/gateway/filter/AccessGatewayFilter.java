package com.mirrorfly.gateway.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.service.TokenService;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 */
@Configuration
@Slf4j
public class AccessGatewayFilter implements GlobalFilter {

	@Autowired
	private TokenService tokenService;

	@Autowired
	private ApplicationProperties applicationProperties;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();
		if (exchange.getRequest().getMethod().equals("OPTIONS")) {
			exchange.getResponse().setStatusCode(HttpStatus.OK);
			return Mono.empty();
		}

		if (exchange.getRequest().getPath().toString().contains("swagger")
				|| exchange.getRequest().getPath().toString().contains("webjars")
				|| exchange.getRequest().getPath().toString().contains("api-docs")
				|| exchange.getRequest().getPath().toString().contains("swagger-resources")
				|| exchange.getRequest().getPath().toString().contains("moderation/userList")
				|| exchange.getRequest().getPath().toString().contains("moderation/groupList")
				|| exchange.getRequest().getPath().toString().contains("moderation/membersList")
				|| exchange.getRequest().getPath().toString().contains("moderation/reportList")
				|| exchange.getRequest().getPath().toString().contains("moderation/media/download")
				|| exchange.getRequest().getPath().toString().contains("moderation/media/delete")
				|| exchange.getRequest().getPath().toString().contains("moderation/media/getDeletedHistoryMessage")
				|| exchange.getRequest().getPath().toString().contains("actuator/health")
				|| exchange.getRequest().getPath().toString().contains("flyadmin/heartbeat")
				|| exchange.getRequest().getPath().toString().contains("/api/v2/auth/initiate")
				|| exchange.getRequest().getPath().toString().contains("/api/v2/auth/verify")
				|| exchange.getRequest().getPath().toString().contains("/api/v2/auth/refresh")
				|| exchange.getRequest().getPath().toString().contains("/api/v2/auth/qr-login")
				|| exchange.getRequest().getPath().toString().contains("/api/v2/auth/xmpp/validate")) {
			ServerHttpRequest.Builder builder = request.mutate();
			return chain.filter(exchange.mutate().request(builder.build()).build());
		}
		boolean isCache = false;

		String header = null;
		if (exchange.getRequest().getPath().toString().contains("media")
				&& exchange.getRequest().getMethod().matches(HttpMethod.GET.toString())
				&& exchange.getRequest().getQueryParams() != null
				&& exchange.getRequest().getQueryParams().containsKey("mf")) {
			List<String> headers = exchange.getRequest().getQueryParams().get("mf");
			if (headers != null && !headers.isEmpty())
				header = headers.get(0);
			isCache = true;
		}
		if (!isCache)
			header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

		if (exchange.getRequest().getPath().toString().contains("group/info")) {
			if (header == null || header.isEmpty()) {
				ServerHttpRequest.Builder builder = request.mutate();
				return chain.filter(exchange.mutate().request(builder.build()).build());
			} else {
				String userdomain = exchange.getRequest().getHeaders().getFirst(GatewayConstants.USER_DOMAIN);
				String username = tokenService.validateToken(userdomain, header);
				if (username == null) {
					return unauthorized(exchange);
				}
				ServerHttpRequest.Builder builder = request.mutate();
				builder.header("username", username);
				builder.header(GatewayConstants.USER_DOMAIN, userdomain);
				return chain.filter(exchange.mutate().request(builder.build()).build());
			}
		}

		if (header == null || header.isEmpty()) {
			return unauthorized(exchange);
		} else {
			String userdomain = exchange.getRequest().getHeaders().getFirst(GatewayConstants.USER_DOMAIN);

			log.info("checking cominggg {}", header);
			log.info("checking cominggg path {}", exchange.getRequest().getPath().toString());

			if (exchange.getRequest().getPath().toString().contains("chat/sendmessage")) {

				log.info("checking cominggg chat/sendmessage  {}", header);

				if (applicationProperties.getAdminSecretKey().equals(header)) {
					log.info("checking cominggg conditon came  {}", header);
					ServerHttpRequest.Builder builder = request.mutate();
					builder.header("username", "123456789");
					return chain.filter(exchange.mutate().request(builder.build()).build());
				}
			}
			
			String username = tokenService.validateToken(userdomain, header);
			if (username == null) {
				return unauthorized(exchange);
			}
			
			ServerHttpRequest.Builder builder = request.mutate();
			builder.header("username", username);
			builder.header(GatewayConstants.USER_DOMAIN, userdomain);
			return chain.filter(exchange.mutate().request(builder.build()).build());
		}
	}

	/**
	 *
	 * @param
	 * @throws IOException
	 */
	private Mono<Void> unauthorized(ServerWebExchange serverWebExchange) {
		try {
			serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			String jsonO = "{ \"status\" : 401, \"message\" : \"Token Expired\", \"error\" : true}";
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(jsonO);
			String gsonObject = actualObj.toString();
			DataBuffer buffer = serverWebExchange.getResponse().bufferFactory().wrap(gsonObject.getBytes());
			return serverWebExchange.getResponse().writeWith(Flux.just(buffer));
		} catch (IOException e) {
			log.error("Error in unauthorized", e.getMessage());
		}
		return null;
	}

}
