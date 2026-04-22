package com.mirrorfly.gateway.config;

import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Swagger3Config {

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new io.swagger.v3.oas.models.info.Info().title("Gateway Service API")
						.description("MirrorFly Gateway Service API Documentation").version("v0.0.1")
						.license(new io.swagger.v3.oas.models.info.License().name("Apache 2.0")
								.url("http://springdoc.org")))
				.components(new Components().addSecuritySchemes("apiKey",
						new SecurityScheme().type(Type.APIKEY).in(In.HEADER).name("Authorization")))
				.addSecurityItem(new io.swagger.v3.oas.models.security.SecurityRequirement().addList("apiKey"));
	}

	@Bean
	public GroupedOpenApi customOpenAPI() {
		return GroupedOpenApi.builder().group("api").packagesToScan("com.mirrorfly.gateway.controller").build();
	}
}
