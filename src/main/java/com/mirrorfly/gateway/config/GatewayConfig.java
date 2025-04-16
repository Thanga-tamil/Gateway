package com.mirrorfly.gateway.config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mirrorfly.gateway.model.ConnectionConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class GatewayConfig {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Bean
	Cache<String, ConnectionConfig> connectionConfigCache() {
		return Caffeine.newBuilder()
				.expireAfterWrite(
						Long.parseLong(Objects.requireNonNullElse(applicationProperties.getCaffeineTtl(), "60")),
						TimeUnit.MINUTES)
				.maximumSize(Long
						.parseLong(Objects.requireNonNullElse(applicationProperties.getCaffeineTotalLimit(), "100")))
				.removalListener(
						(key, value, cause) -> log.info("Entry removed: key={}, value={}, cause={}", key, value, cause))
				.build();
	}

	@Bean
	TemplateEngine customTemplateEngine() {
		TemplateEngine templateEngine = new TemplateEngine();

		StringTemplateResolver templateResolver = new StringTemplateResolver();
		templateResolver.setTemplateMode(TemplateMode.HTML);
		templateResolver.setCacheable(false);

		templateEngine.setTemplateResolver(templateResolver);

		return templateEngine;
	}

}
