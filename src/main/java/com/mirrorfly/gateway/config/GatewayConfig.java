package com.mirrorfly.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class GatewayConfig {

	@Bean
	TemplateEngine customTemplateEngine() {
		return new TemplateEngine() {
			{
				setTemplateResolver(new StringTemplateResolver() {
					{
						setTemplateMode(TemplateMode.HTML);
						setCacheable(false);
					}
				});
			}
		};
	}

}
