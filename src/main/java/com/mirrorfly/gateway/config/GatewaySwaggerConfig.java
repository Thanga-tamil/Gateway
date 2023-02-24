package com.mirrorfly.gateway.config;

import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;

import com.fasterxml.jackson.databind.ObjectMapper;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Profile(value = { "!production" })
@Configuration
public class GatewaySwaggerConfig{
	
	private static final String basePackage = "com.mirrorfly.gateway";

	@Bean
	public Jackson2JsonEncoder jackson2JsonEncoder(ObjectMapper mapper) {
		return new Jackson2JsonEncoder(mapper);
	}

	@Bean
	public Jackson2JsonDecoder jackson2JsonDecoder(ObjectMapper mapper) {
		return new Jackson2JsonDecoder(mapper);
	}

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select()
				.apis(RequestHandlerSelectors.basePackage(basePackage)).paths(PathSelectors.any()).build()
				.apiInfo(apiEndPointsInfo());
	}
	
	@Bean
	  public Docket jsonApi1() {
	      return new Docket(SWAGGER_2)
	          .groupName("Api Documentation 1.0")
	          .select()
	          .apis(RequestHandlerSelectors.basePackage(basePackage))
	              .paths(PathSelectors.ant("api/v1/gateway/**"))
	          .build();
	  }
	
	@Bean
	  public Docket jsonApi2() {
	      return new Docket(SWAGGER_2)
	          .groupName("Api Documentation 2.0")
	          .select()
	          .apis(RequestHandlerSelectors.basePackage(basePackage))
	              .paths(PathSelectors.ant("api/v2/gateway/**"))
	          .build();
	  }

	private ApiInfo apiEndPointsInfo() {
		return new ApiInfoBuilder().title("REST API").description(" REST API for Chat Service")
				.contact(new Contact("", "", "")).license("Apache 2.0")
				.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html").version("1.0.0").build();
	}

}