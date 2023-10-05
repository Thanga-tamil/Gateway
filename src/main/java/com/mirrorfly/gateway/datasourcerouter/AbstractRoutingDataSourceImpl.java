package com.mirrorfly.gateway.datasourcerouter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.util.DataSourceUtil;
import com.mirrorfly.gateway.util.GatewayUtil;
import com.zaxxer.hikari.HikariDataSource;

public class AbstractRoutingDataSourceImpl extends AbstractRoutingDataSource {

	private static final ThreadLocal<String> DATABASE_NAME = new ThreadLocal<>();
	private static DataSource defaultDS = null;
	private static Map<String, DataSource> allDataSources = new HashMap<>();

	@Autowired
	private DataSourceUtil dataSourceUtil;

	public AbstractRoutingDataSourceImpl() {
	}

	public AbstractRoutingDataSourceImpl(DataSource defaultTargetDatasource, Map<Object, Object> targetDatasources) {
		this.setDefaultTargetDataSource(defaultTargetDatasource);
		this.setTargetDataSources(targetDatasources);
		this.afterPropertiesSet();
		setDefaultDataSource(defaultTargetDatasource);
		setDefaultDataSources(targetDatasources);
	}

	public static void setDatabaseName(String key) {
		DATABASE_NAME.set(key);
	}

	public static String getDatabaseName() {
		return DATABASE_NAME.get();
	}

	public static void removeDatabaseName() {
		DATABASE_NAME.remove();
	}

	@Override
	protected Object determineCurrentLookupKey() {
		return DATABASE_NAME.get();
	}

	public static void setDefaultDataSource(DataSource ds) {
		defaultDS = ds;
	}

	public static DataSource getDefaultDataSource() {
		return defaultDS;
	}

	public static void setDefaultDataSources(Map<Object, Object> dataSources) {
		for (Map.Entry<Object, Object> entry : dataSources.entrySet()) {
			String key = (String) entry.getKey();
			DataSource ds = (DataSource) entry.getValue();
			allDataSources.put(key, ds);
		}
	}

	public static DataSource getActiveDataSource() {
		return allDataSources.get(DATABASE_NAME.get());
	}

	public void refresh(List<ConnectionConfig> connectionConfigs) {
		for (ConnectionConfig config : connectionConfigs) {
			if (allDataSources.containsKey(config.getApiDomain())) {
				HikariDataSource ds = (HikariDataSource) allDataSources.get(config.getApiDomain());
				// Reload only if any of connection params has changed
				if (!isConfigurationSame(ds, config)) {
					ds.close();
					allDataSources.put(config.getApiDomain(), dataSourceUtil.createDataSource(config));
				}
			} else {
				// New data source, just create and store it.
				allDataSources.put(config.getApiDomain(), dataSourceUtil.createDataSource(config));
			}
		}
	}

	private boolean isConfigurationSame(HikariDataSource dataSource, ConnectionConfig configuration) {
		Map<String, Object> mysql = GatewayUtil.convertJsonStringToMap(configuration.getMysql());
		return Objects.equals(dataSource.getJdbcUrl(), mysql.get("connection_string"))
				&& Objects.equals(dataSource.getUsername(), mysql.get("username"))
				&& Objects.equals(dataSource.getPassword(), mysql.get("password"))
				&& Objects.equals(dataSource.getMinimumIdle(), mysql.get("minimum_connections"))
				&& Objects.equals(dataSource.getMaximumPoolSize(), mysql.get("maximum_connections"));
	}

	@Override
	protected DataSource determineTargetDataSource() {
		String lookupKey = (String) determineCurrentLookupKey();

		if (lookupKey == null || lookupKey.trim().length() == 0) {
			lookupKey = "default";
		}

		return allDataSources.get(lookupKey);
	}

	public static Set<String> getAvailableDataSourcesKeys() {
		Set<String> keys = new HashSet<>();
		for (String key : allDataSources.keySet()) {
			keys.add(key);
		}
		return keys;
	}

	public static void updateDataSources(Map<String, DataSource> newDataSources) {
		if (newDataSources != null && newDataSources.size() > 0) {
			for (Map.Entry<String, DataSource> entry : newDataSources.entrySet()) {
				allDataSources.put(entry.getKey(), entry.getValue());
			}
		}
	}

}