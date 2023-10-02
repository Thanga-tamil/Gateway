package com.mirrorfly.gateway.util;

import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataSourceUtil {

	@Autowired
	private ApplicationProperties applicationProperties;

	public HikariDataSource createDataSource(ConnectionConfig connectionConfig) {
		final HikariConfig config = new HikariConfig();
		if (connectionConfig == null) {
			// default datasource
			config.setPoolName("CP_Default");
			config.setJdbcUrl(applicationProperties.getConnectionString());
			config.setUsername(applicationProperties.getDbUsername());
			config.setPassword(applicationProperties.getDbPassword());
			config.setMinimumIdle(applicationProperties.getMinimumConnections());
			config.setMaximumPoolSize(applicationProperties.getMaximumConnections());
		} else {
			config.setPoolName("CP_" + connectionConfig.getApiDomain());
			Map<String, Object> mysql = GatewayUtil.convertJsonStringToMap(connectionConfig.getMysql());
			config.setJdbcUrl(requireNonEmptyValue(mysql.get("connection_string")));
			config.setUsername(requireNonEmptyValue(mysql.get("username")));
			config.setPassword(requireNonEmptyValue(mysql.get("password")));
			config.setMinimumIdle(parseIntegerOrDefault(mysql.get("minimum_connections"),
					applicationProperties.getMinimumConnections()));
			config.setMaximumPoolSize(parseIntegerOrDefault(mysql.get("maximum_connections"),
					applicationProperties.getMaximumConnections()));
		}
		config.setIdleTimeout(applicationProperties.getConnectionIdleTimeout());
		config.setMaxLifetime(applicationProperties.getConnectionMaxLifeTime());
		// Performance Tips for MySql
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		return new HikariDataSource(config);
	}

	private String requireNonEmptyValue(Object value) {
		if (StringUtils.isNotBlank(value.toString())) {
			return value.toString();
		} else {
			throw new IllegalArgumentException("Missing Datasouce values");
		}
	}

	private Integer parseIntegerOrDefault(Object value, Integer defaultValue) {
		try {
			return Optional.ofNullable(value).map(Object::toString).filter(StringUtils::isNotBlank)
					.map(Integer::parseInt).orElse(defaultValue);
		} catch (NumberFormatException e) {
			log.error("NumberFormatException: {}", e.getMessage());
			return defaultValue;
		}
	}

}
