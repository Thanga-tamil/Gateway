package com.mirrorfly.gateway.util;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

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
			Map<String, Object> mysql = GatewayUtil.getMapData(connectionConfig.getMysql());
			config.setJdbcUrl(mysql.get("connection_string").toString());
			config.setUsername(mysql.get("username").toString());
			config.setPassword(mysql.get("password").toString());
			config.setMinimumIdle(Integer.parseInt(mysql.get("minimum_connections").toString()));
			config.setMaximumPoolSize(Integer.parseInt(mysql.get("maximum_connections").toString()));
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

}
