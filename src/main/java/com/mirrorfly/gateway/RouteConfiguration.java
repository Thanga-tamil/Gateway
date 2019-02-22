package com.mirrorfly.gateway;

import java.util.Arrays;
import java.util.HashMap;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.discovery.DiscoveryClientRouteDefinitionLocator;
import org.springframework.cloud.gateway.discovery.DiscoveryLocatorProperties;
import org.springframework.cloud.gateway.handler.RoutePredicateHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class RouteConfiguration {

	@Bean
	DiscoveryClientRouteDefinitionLocator discoveryRoutes(DiscoveryClient dc, DiscoveryLocatorProperties dlp) {
		return new DiscoveryClientRouteDefinitionLocator(dc, dlp);
	}

	@Bean
	public CorsConfiguration corsConfiguration(RoutePredicateHandlerMapping routePredicateHandlerMapping) {
		CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues();
		Arrays.asList(HttpMethod.OPTIONS, HttpMethod.PUT, HttpMethod.GET, HttpMethod.DELETE, HttpMethod.POST)
				.forEach(m -> corsConfiguration.addAllowedMethod(m));
		corsConfiguration.addAllowedOrigin("*");
		corsConfiguration.addAllowedHeader("*");
		routePredicateHandlerMapping.setCorsConfigurations(new HashMap<String, CorsConfiguration>() {
			{
				put("/**", corsConfiguration);
			}
		});
		return corsConfiguration;
	}

}
