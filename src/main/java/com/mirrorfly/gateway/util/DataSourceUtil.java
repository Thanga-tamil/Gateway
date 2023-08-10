package com.mirrorfly.gateway.util;

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
		if(connectionConfig == null) {
			// default datasource
			config.setPoolName("CP_Default");
			config.setJdbcUrl(applicationProperties.getConnectionString());
			config.setUsername(applicationProperties.getDbUsername());
			config.setPassword(applicationProperties.getDbPassword());
			config.setMinimumIdle(applicationProperties.getMinimumConnections());
	        config.setMaximumPoolSize(applicationProperties.getMaximumConnections());
		}else {
			config.setPoolName("CP_"+connectionConfig.getCustomerDomain());
			config.setJdbcUrl(connectionConfig.getConnectionString());
			config.setUsername(connectionConfig.getDbUsername());
			config.setPassword(connectionConfig.getDbPassword());
	        config.setMinimumIdle(connectionConfig.getMinimumConnections());
	        config.setMaximumPoolSize(connectionConfig.getMaximumConnections());
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
