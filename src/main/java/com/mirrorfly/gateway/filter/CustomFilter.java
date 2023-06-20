package com.mirrorfly.gateway.filter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.logging.MDC;
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
			test(request);
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
	
	private void test(ServerHttpRequest request){
		log.info(" <<<================================== ");
		URL url = null;
		try {
			url = request.getURI().toURL();
			log.info("url ------> " + url.toString());
			log.info("url.getRef() ------> " + url.getRef());
			log.info("url.getHost() ------> " + url.getHost());
			log.info("url.getPort() ------> " + url.getPort());
			log.info("url.getProtocol() ------> " + url.getProtocol());
			log.info("url.getAuthority() ------> " + url.getAuthority());
			log.info("request.getPath() ------> " + request.getPath());
			log.info("request.contextPath() ------> " + request.getPath().contextPath().value());
			log.info("request.getSslInfo() ------> " + request.getSslInfo());
			
			InetSocketAddress localAddress = request.getLocalAddress();
			if(localAddress!=null) {
				log.info("localAddress.getAddress ------> " + localAddress.getAddress());
				log.info("localAddress.getHostName ------> " + localAddress.getHostName());
				log.info("localAddress.getHostString ------> " + localAddress.getHostString());
				log.info("localAddress.getPort ------> " + localAddress.getPort());
			}
			
			InetSocketAddress remoteAddress = request.getRemoteAddress();
			if(remoteAddress!=null) {
				log.info("remoteAddress.getAddress ------> " + remoteAddress.getAddress());
				log.info("remoteAddress.getHostName ------> " + remoteAddress.getHostName());
				log.info("remoteAddress.getHostString ------> " + remoteAddress.getHostString());
				log.info("remoteAddress.getPort ------> " + remoteAddress.getPort());
			}
			
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		log.info(" ==================================>>> ");
	}

}
