package com.mirrorfly.gateway.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

@Component
@Primary
public class SwaggerAggregatorController implements SwaggerResourcesProvider {
	@Override
	public List<SwaggerResource> get() {
		return Arrays.asList(swaggerResource("Login Service", "/v2/api-docs"),
				swaggerResource("chatservice", "/chat/v2/api-docs"));
	}

	private SwaggerResource swaggerResource(String name, String location) {
		SwaggerResource swaggerResource = new SwaggerResource();
		swaggerResource.setName(name);
		swaggerResource.setLocation(location);
		swaggerResource.setSwaggerVersion("2.0");
		return swaggerResource;
	}

}
