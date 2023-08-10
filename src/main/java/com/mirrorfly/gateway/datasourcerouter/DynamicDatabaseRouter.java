package com.mirrorfly.gateway.datasourcerouter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.util.DataSourceUtil;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = "com.mirrorfly.gateway", transactionManagerRef = "transcationManager", entityManagerFactoryRef = "entityManager")
@EnableTransactionManagement
public class DynamicDatabaseRouter {

    @Autowired
    private DataSourceUtil dataSourceUtil;
    
    @Bean
    @Primary
    @Scope("singleton")
    public AbstractRoutingDataSourceImpl dataSource() {
        Map<Object, Object> targetDataSources = getTargetDataSources();
        return new AbstractRoutingDataSourceImpl((DataSource)targetDataSources.get("default"), targetDataSources);
    }

    @Bean(name = "entityManager")
    @Scope("singleton")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(EntityManagerFactoryBuilder builder) {
        return builder.dataSource(dataSource()).packages("com.mirrorfly.gateway").build();
    }

    @Bean(name = "transcationManager")
    @Scope("singleton")
    public JpaTransactionManager transactionManager(
            @Autowired @Qualifier("entityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaTransactionManager(entityManagerFactoryBean.getObject());
    }
    
    private Map<Object,Object> getTargetDataSources() {
    	Map<Object,Object> targetDataSourceMap = new HashMap<>();
    	
    	HikariDataSource defaultDS = dataSourceUtil.createDataSource(null);
    	log.info("Default DataSource is -----> "+defaultDS.getJdbcUrl());
    	targetDataSourceMap.put("default", defaultDS);
        List<ConnectionConfig> connectionConfigs = getConnectionConfigs(defaultDS);
        for (ConnectionConfig connectionConfig : connectionConfigs) {
            HikariDataSource domainSpecificDS = dataSourceUtil.createDataSource(connectionConfig);
            targetDataSourceMap.put(connectionConfig.getCustomerDomain(), domainSpecificDS);
            log.info("[{}] DataSource is [{}] -----> ",connectionConfig.getCustomerDomain(), domainSpecificDS.getJdbcUrl());
		}
        return targetDataSourceMap;
    }
    
	private List<ConnectionConfig> getConnectionConfigs(HikariDataSource defaultDS) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<ConnectionConfig> list = new ArrayList<>();
		try {
			connection = defaultDS.getConnection();
			String sql = "SELECT * FROM connection_config";

			ps = connection.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				ConnectionConfig config = new ConnectionConfig();
				config.setId(rs.getInt("id"));
				config.setCustomerDomain(rs.getString("customer_domain"));
				config.setConnectionString(rs.getString("connection_string"));
				config.setDbUsername(rs.getString("db_username"));
				config.setDbPassword(rs.getString("db_password"));
				config.setMinimumConnections(rs.getInt("minimum_connections"));
				config.setMaximumConnections(rs.getInt("maximum_connections"));
				list.add(config);
			}
		} catch (SQLException e) {
			log.error("SQLException occured while fetching configured connection details from connection_config ::: " + e);
		} finally {
			releaseResources(connection, ps, rs);
		}
		return list;
	}
	
	private void releaseResources(Connection con, PreparedStatement ps, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			log.error("SQLException occured while closing ResultSet ::: Supressing this exception ::: " + e);
		}

		try {
			if (ps != null) {
				ps.close();
			}
		} catch (SQLException e) {
			log.error("SQLException occured while closing PreparedStatement ::: Supressing this exception ::: " + e);
		}

		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			log.error("SQLException occured while closing Connection ::: Supressing this exception ::: " + e);
		}
	}

}