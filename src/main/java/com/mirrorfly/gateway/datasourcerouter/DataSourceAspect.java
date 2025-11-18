package com.mirrorfly.gateway.datasourcerouter;

import java.lang.reflect.Method;
import java.util.Map;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mirrorfly.gateway.exception.DataSourceNotFoundException;
import com.mirrorfly.gateway.service.InstantRefresh;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(-10)
public class DataSourceAspect {

	@Autowired
	private ApplicationContext applicationContext;

	@Pointcut("@annotation(com.mirrorfly.gateway.datasourcerouter.SwitchDataSource)")
	public void annotationPointCut() {
		// default function
		// This method serves as a pointcut expression for the SwitchDataSource
		// annotation.
		// It is empty because it is used as a placeholder for the pointcut.
		// The actual logic is implemented in the @Before and @After advice methods.
	}

	@Before("annotationPointCut()")
	public void before(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		MethodSignature sign = (MethodSignature) joinPoint.getSignature();
		Method method = sign.getMethod();
		SwitchDataSource annotation = method.getAnnotation(SwitchDataSource.class);
		if (annotation != null) {
			String targetDs = (String) args[0];
			AbstractRoutingDataSourceImpl.setDatabaseName(targetDs);
			DataSource ds = AbstractRoutingDataSourceImpl.getActiveDataSource();
			if (ds == null) {
				InstantRefresh instantRefresh = new InstantRefresh();
				AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext
						.getAutowireCapableBeanFactory();
				autowireCapableBeanFactory.autowireBean(instantRefresh);
				Map<String, DataSource> newDataSources = instantRefresh.refresh(
						AbstractRoutingDataSourceImpl.getDefaultDataSource(),
						AbstractRoutingDataSourceImpl.getAvailableDataSourcesKeys());
				AbstractRoutingDataSourceImpl.updateDataSources(newDataSources);

				ds = AbstractRoutingDataSourceImpl.getActiveDataSource();

				if (ds == null)
					throw new DataSourceNotFoundException("No datasource is configured for domain " + targetDs);
			}
			log.info("Switch DataSource for [{}] in Method [{}]", targetDs, joinPoint.getSignature());

			HikariDataSource defaultDS = (HikariDataSource) AbstractRoutingDataSourceImpl.getDefaultDataSource();
			HikariDataSource activeDS = (HikariDataSource) AbstractRoutingDataSourceImpl.getActiveDataSource();
			log.info("Default DataSource is [{}]", defaultDS.getJdbcUrl());
			log.info("Active DataSource is [{}]", activeDS.getJdbcUrl());
		}
	}

}
