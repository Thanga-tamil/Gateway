package com.mirrorfly.gateway.datasourcerouter;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class AbstractRoutingDataSourceImpl extends AbstractRoutingDataSource {

    private static final ThreadLocal<String> DATABASE_NAME = new ThreadLocal<>();
    private static DataSource defaultDS = null;
    private static Map<String,DataSource> allDataSources = new HashMap<>();

    public AbstractRoutingDataSourceImpl(DataSource defaultTargetDatasource, Map<Object,Object> targetDatasources) {
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
    
    public static void setDefaultDataSources(Map<Object,Object> dataSources) {
    	for (Map.Entry<Object, Object> entry : dataSources.entrySet()) {
    		String key = (String) entry.getKey();
			DataSource ds = (DataSource) entry.getValue();
			allDataSources.put(key, ds);
		}
    }
    
    public static DataSource getActiveDataSource() {
    	return allDataSources.get(DATABASE_NAME.get());
    }
    
    
   
}