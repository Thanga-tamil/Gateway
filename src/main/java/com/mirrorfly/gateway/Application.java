package com.mirrorfly.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import reactor.core.publisher.Mono;

@SpringBootApplication
public class Application {

	/**
	 * This main class act as an starting point of all application.
	 * 
	 * @param args array.
	 */
	public static void main(String[] args) {

		SpringApplication application = new SpringApplication(Application.class);
		application.setWebApplicationType(WebApplicationType.REACTIVE);
		application.run(args);

	}

	/**
	 * This method allows the specific Cors Request like GET, POST, PUT, DELETE,
	 * OPTIONS. other than Mapping functions are not allowed here.
	 * 
	 * @return http response
	 */
	@Bean
	public CorsFilter corsFilter() {
		final UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
		final CorsConfiguration corsConfig = new CorsConfiguration();
		corsConfig.setAllowCredentials(true);
		corsConfig.addAllowedOrigin("*");
		corsConfig.addAllowedHeader("*");
		corsConfig.addAllowedMethod("OPTIONS");
		corsConfig.addAllowedMethod("HEAD");
		corsConfig.addAllowedMethod("GET");
		corsConfig.addAllowedMethod("PUT");
		corsConfig.addAllowedMethod("POST");
		corsConfig.addAllowedMethod("DELETE");
		corsConfig.addAllowedMethod("PATCH");
		urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfig);
		return new CorsFilter(urlBasedCorsConfigurationSource);
	}

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}


}
