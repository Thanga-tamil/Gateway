package com.mirrorfly.gateway.service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataStore {

    private final CacheManager cacheManager;

    // lazy-initialized, reused EntityManagerFactory for master DB
    private volatile EntityManagerFactory masterEntityManagerFactory;

    public void setConnectionConfig(List<ConnectionConfig> config) {
        cacheManager.invalidateAll(); // Clear the cache intentionally on full replace
        config.forEach(data -> cacheManager.put(data.getApiDomain(), data));
        log.info("ConnectionConfig loaded into cache. Size is : {}", cacheManager.estimatedSize());
    }

    public void reloadConnectionConfig() {

        // Clear the cache
        cacheManager.invalidateAll();

        EntityManagerFactory entityManagerFactory = getOrCreateMasterEntityManagerFactory();
        EntityManager entityManager = null;
        try {
            entityManager = entityManagerFactory.createEntityManager();

            // Fetch all ConnectionConfig entities (only use when necessary)
            List<ConnectionConfig> configs = entityManager.createQuery("SELECT c FROM ConnectionConfig c", ConnectionConfig.class).getResultList();

            // Put each ConnectionConfig into the cache
            configs.forEach(config -> cacheManager.put(config.getApiDomain(), config));

            log.info("ConnectionConfig cache refreshed. Size is: {}", cacheManager.estimatedSize());
        } catch (Exception e) {
            log.error("Error while reloading ConnectionConfig", e);
        } finally {
            if (entityManager != null && entityManager.isOpen()) {
                entityManager.close();
            }
        }
    }
    public EntityManagerFactory createMasterEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();

        factoryBean.setDataSource(AbstractRoutingDataSourceImpl.getDefaultDataSource()); // Master DB Connection
        factoryBean.setPackagesToScan("com.mirrorfly.gateway.model");
        factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "none");
        jpaProperties.put("hibernate.format_sql", "true");
        jpaProperties.put("hibernate.show_sql", "true");
        jpaProperties.put("hibernate.jdbc.batch_size", "30");

        factoryBean.setJpaProperties(jpaProperties);
        factoryBean.setPersistenceUnitName("masterPU");
        factoryBean.afterPropertiesSet();

        return factoryBean.getObject();
    }

    public ConnectionConfig getConnectionConfigByDomain(String domain) {

        ConnectionConfig config = cacheManager.getIfPresent(domain);

        // If the config is not found in the cache, reload it
        if (config == null) {
            log.info("Could not find ConnectionConfig data for domain {} in cache. Reloading connection_config table.",
                    domain);
            config = reloadConnectionConfigForDomain(domain);
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

    public ConnectionConfig reloadConnectionConfigForDomain(String domain) {

        EntityManagerFactory emf = getOrCreateMasterEntityManagerFactory();
        EntityManager em = null;
        try {
            em = emf.createEntityManager();
            TypedQuery<ConnectionConfig> q = em.createQuery(
                    "SELECT c FROM ConnectionConfig c WHERE c.apiDomain = :domain", ConnectionConfig.class);
            q.setParameter("domain", domain);
            ConnectionConfig cfg = q.getSingleResult();
            if (cfg != null) {
                cacheManager.put(domain, cfg);
                log.info("Loaded ConnectionConfig for domain {} into cache", domain);
            }
            return cfg;
        } catch (NoResultException nr) {
            log.warn("No ConnectionConfig found in master DB for domain {}", domain);
            return null;
        } catch (Exception e) {
            log.error("Error while loading ConnectionConfig for domain {}", domain, e);
            return null;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Lazily create and return the master EntityManagerFactory.
     * This ensures we don't recreate EMF every time.
     */
    private EntityManagerFactory getOrCreateMasterEntityManagerFactory() {
        if (masterEntityManagerFactory == null) {
            synchronized (this) {
                if (masterEntityManagerFactory == null) {
                    masterEntityManagerFactory = createMasterEntityManagerFactory();
                    log.info("Created master EntityManagerFactory for masterPU");
                }
            }
        }
        return masterEntityManagerFactory;
    }
}