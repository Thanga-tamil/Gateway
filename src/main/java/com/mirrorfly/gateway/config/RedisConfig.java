package com.mirrorfly.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfig {

	@Autowired
	private ApplicationProperties applicationProperties;

	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration redisConf = new RedisStandaloneConfiguration();
		redisConf.setHostName(applicationProperties.getRedisHost());
		redisConf.setPassword(RedisPassword.of(applicationProperties.getRedisPassword()));
		redisConf.setPort(Integer.parseInt(applicationProperties.getRedisPort()));
		return new LettuceConnectionFactory(redisConf);
	}

	/*
	 * @Bean public ReactiveRedisTemplate<String, RedisChatToken>
	 * reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
	 * StringRedisSerializer keySerializer = new StringRedisSerializer();
	 * Jackson2JsonRedisSerializer<RedisChatToken> valueSerializer = new
	 * Jackson2JsonRedisSerializer<>( RedisChatToken.class);
	 * RedisSerializationContext.RedisSerializationContextBuilder<String,
	 * RedisChatToken> builder = RedisSerializationContext
	 * .newSerializationContext(keySerializer); RedisSerializationContext<String,
	 * RedisChatToken> context = builder.value(valueSerializer).build(); return new
	 * ReactiveRedisTemplate<>(factory, context); }
	 * 
	 * @Bean public RedisTemplate<String, RedisChatToken> redisTemplate() {
	 * RedisTemplate<String, RedisChatToken> redisTemplate = new RedisTemplate<>();
	 * redisTemplate.setConnectionFactory(redisConnectionFactory()); return
	 * redisTemplate; }
	 */

	@Bean
	public StringRedisTemplate stringRedisTemplate() {
		StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(redisConnectionFactory());
		stringRedisTemplate.setEnableTransactionSupport(true);
		return stringRedisTemplate;
	}

}
