package com.mirrorfly.gateway.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.cachemanager.CacheManager;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.model.ConnectionConfig;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DataStore {

	private final CacheManager cacheManager;

	@Autowired
	public DataStore(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	public void setConnectionConfig(List<ConnectionConfig> config) {
		cacheManager.invalidateAll(); // Clear the cache
		config.forEach(data -> cacheManager.put(data.getApiDomain(), data));
		log.info("ConnectionConfig loaded into Caffeine cache. Size is : " + cacheManager.estimatedSize());
	}

	public void reloadConnectionConfig() {

		// Clear the cache
		cacheManager.invalidateAll();

		EntityManagerFactory entityManagerFactory = createEntityManagerFactory();
		EntityManager entityManager = entityManagerFactory.createEntityManager();

		try {
			// Fetch all ConnectionConfig entities
			List<ConnectionConfig> configs = entityManager.createQuery(
					"SELECT c FROM ConnectionConfig c", ConnectionConfig.class)
					.getResultList();

			// Put each ConnectionConfig into the cache
			configs.forEach(config -> cacheManager.put(config.getApiDomain(), config));

			log.info("ConnectionConfig cache refreshed. Size is: {}", cacheManager.estimatedSize());
		} catch (Exception e) {
			log.error("Error while reloading ConnectionConfig", e);
		} finally {
			if (entityManager != null && entityManager.isOpen()) {
				entityManager.close();
				log.info("EntityManager closed.");
			}
		}
	}

	private EntityManagerFactory createEntityManagerFactory() {

		DataSource dataSource = AbstractRoutingDataSourceImpl.getDefaultDataSource();

		// Create a LocalContainerEntityManagerFactoryBean
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setPackagesToScan("com.mirrorfly.gateway.model");
		factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

		Properties jpaProperties = new Properties();
		jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
		jpaProperties.put("hibernate.hbm2ddl.auto", "none");
		jpaProperties.put("hibernate.show_sql", "true");
		jpaProperties.put("hibernate.format_sql", "true");
		factoryBean.setJpaProperties(jpaProperties);

		factoryBean.afterPropertiesSet();

		return factoryBean.getObject();
	}

	public ConnectionConfig getConnectionConfigByDomain(String domain) {

		ConnectionConfig config = cacheManager.getIfPresent(domain);

		// If the config is not found in the cache, reload it
		if (config == null) {
			log.info("Could not find ConnectionConfig data for domain {} in cache. Reloading connection_config table.",
					domain);
			reloadConnectionConfig();
			config = cacheManager.getIfPresent(domain);
		}

		return config;
	}

	public Object getConnectionConfigDataByDomainAndKey(String domain, String key) {
		ConnectionConfig config = getConnectionConfigByDomain(domain);
		Map<String, Object> configAsMap = new HashMap<>();
		if (config != null) {
			ObjectMapper mapper = new ObjectMapper();
			configAsMap = mapper.convertValue(config, new TypeReference<Map<String, Object>>() {
			});
		}
		return configAsMap.get(key);
	}

}
