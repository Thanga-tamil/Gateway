package com.mirrorfly.gateway.filter;

import java.net.InetSocketAddress;

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
public class IpWhitelistingFilter implements WebFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		boolean isRunningInKubernetes = isRunningInKubernetes();
		log.info("Is running in Kubernetes? " + isRunningInKubernetes);
		ServerHttpRequest.Builder builder = setHeaders(exchange.getRequest());
		return chain.filter(exchange.mutate().request(builder.build()).build());
	}

	@Override
	public int getOrder() {
		return Ordered.HIGHEST_PRECEDENCE;
	}

	private ServerHttpRequest.Builder setHeaders(ServerHttpRequest request) {
		try {
			InetSocketAddress address = request.getRemoteAddress();
			String remoteIPAddress = "";
			if (address != null && address.getAddress() != null) {
				remoteIPAddress = address.getAddress().toString().replace("/", "");
				log.info("Remote IP Address ------> " + remoteIPAddress);
				log.info("Host IP Address ------> " + address.getAddress().getHostAddress());
			}
			ServerHttpRequest.Builder builder = request.mutate();
			builder.header(GatewayConstants.REMOTE_IP, remoteIPAddress);
			log.info("Setting "+GatewayConstants.REMOTE_IP+" in header ------> " + remoteIPAddress);
			return builder;
		} catch (Exception e) {
			log.error("Remote IP Address fetching error ---> " + e.getMessage());
			throw new BadDataException("Unable to identify the remote ip from request");
		}
	}
	
    private boolean isRunningInKubernetes() {
    	try {
    		String kubernetesServiceHost = System.getenv("KUBERNETES_SERVICE_HOST");
            String kubernetesServicePort = System.getenv("KUBERNETES_SERVICE_PORT");
            return kubernetesServiceHost != null && kubernetesServicePort != null;
    	}catch(Exception e) {
    		log.error("Could not read environment variables, KUBERNETES_SERVICE_HOST and KUBERNETES_SERVICE_PORT "+e.getMessage());
    		return false;
    	}
    }

}
