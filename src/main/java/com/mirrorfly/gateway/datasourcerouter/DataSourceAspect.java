package com.mirrorfly.gateway.datasourcerouter;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.mirrorfly.gateway.exception.DataSourceNotFoundException;
import com.zaxxer.hikari.HikariDataSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(-10)
public class DataSourceAspect {

    @Pointcut("@annotation(com.mirrorfly.gateway.datasourcerouter.SwitchDataSource)")
    public void annotationPointCut() {
        //default function
    }

    
    @Before("annotationPointCut()")
    public void before(JoinPoint joinPoint){
    	Object[] args = joinPoint.getArgs();
        MethodSignature sign =  (MethodSignature)joinPoint.getSignature();
        Method method = sign.getMethod();
        SwitchDataSource annotation = method.getAnnotation(SwitchDataSource.class);
        if(annotation != null){
        	String targetDs = (String)args[0];
        	AbstractRoutingDataSourceImpl.setDatabaseName(targetDs);
        	DataSource ds = AbstractRoutingDataSourceImpl.getActiveDataSource();
        	if(ds == null) {
        		throw new DataSourceNotFoundException("No datasource is configured for domain "+targetDs);
        	}
        	log.info("Switch DataSource for [{}] in Method [{}]",
            		targetDs, joinPoint.getSignature());
            
        	HikariDataSource defaultDS = (HikariDataSource) AbstractRoutingDataSourceImpl.getDefaultDataSource();
        	HikariDataSource activeDS = (HikariDataSource) AbstractRoutingDataSourceImpl.getActiveDataSource();
    		log.info("Default DataSource is [{}]", defaultDS.getJdbcUrl());
    		log.info("Active DataSource is [{}]",  activeDS.getJdbcUrl());
        }
    }
    
    @After("annotationPointCut()")
    public void after(JoinPoint point){
        if(null != AbstractRoutingDataSourceImpl.getDatabaseName()) {
        	
        	AbstractRoutingDataSourceImpl.removeDatabaseName();
        	AbstractRoutingDataSourceImpl.setDatabaseName("default");
        	
        	log.info("Restore DataSource to [{}] in Method [{}]",
                    AbstractRoutingDataSourceImpl.getDatabaseName(), point.getSignature());
        	/*
        	try{
        		HikariDataSource defaultDS = (HikariDataSource) AbstractRoutingDataSourceImpl.getDefaultDataSource();
            	HikariDataSource activeDS = (HikariDataSource) AbstractRoutingDataSourceImpl.getActiveDataSource();
        		log.info("Default DataSource is [{}]", defaultDS.getJdbcUrl());
        		log.info("Active DataSource is [{}]",  activeDS.getJdbcUrl());
        	}catch (Exception e) {}
        	*/
        }
    }
    
}