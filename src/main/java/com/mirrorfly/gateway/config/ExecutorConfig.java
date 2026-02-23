package com.mirrorfly.gateway.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ExecutorConfig {

	@Bean(name = "contactsyncExecutor")
	public Executor customAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(20);
		executor.setQueueCapacity(100);
		executor.setThreadNamePrefix("ContactAsync-");
		executor.initialize();
		return executor;
	}

	@Bean(name = "loginExecutor")
	public Executor loginExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(10);
		executor.setMaxPoolSize(50);
		executor.setQueueCapacity(300);
		executor.setThreadNamePrefix("LoginAsync-");
		executor.initialize();
		return executor;
	}

}
