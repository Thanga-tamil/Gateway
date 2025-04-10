package com.mirrorfly.gateway.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfig {

    @Autowired
	private ApplicationProperties applicationProperties;

    @Bean(name = "defaultDataSource")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
                .url(applicationProperties.getConnectionString())
                .username(applicationProperties.getDbUsername())
                .password(applicationProperties.getDbPassword())
                .driverClassName("com.mysql.cj.jdbc.Driver")
                .build();
    }
}
