package com.mirrorfly.gateway.datasourcerouter;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(-10)
public class DataSourceAspect {
    private final Logger logger = LoggerFactory.getLogger(getClass());

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
            logger.info("Switch DataSource for [{}] in Method [{}]",
            		targetDs, joinPoint.getSignature());
            /*
        	DataSource defaultDS = AbstractRoutingDataSourceImpl.getDefaultDataSource();
        	DataSource activeDS = AbstractRoutingDataSourceImpl.getActiveDataSource();
        	
        	try{
        		logger.info("Default DataSource is [{}]", defaultDS.getConnection().getMetaData().getURL());
        		logger.info("Active DataSource is [{}]",  activeDS.getConnection().getMetaData().getURL());
        	}catch (Exception e) {}
        	*/
        }
    }
    
    @After("annotationPointCut()")
    public void after(JoinPoint point){
        if(null != AbstractRoutingDataSourceImpl.getDatabaseName()) {
        	
        	AbstractRoutingDataSourceImpl.removeDatabaseName();
        	AbstractRoutingDataSourceImpl.setDatabaseName("default");
        	
        	logger.info("Restore DataSource to [{}] in Method [{}]",
                    AbstractRoutingDataSourceImpl.getDatabaseName(), point.getSignature());
        	/*
        	DataSource defaultDS = AbstractRoutingDataSourceImpl.getDefaultDataSource();
        	DataSource activeDS = AbstractRoutingDataSourceImpl.getActiveDataSource();
        	
        	try{
        		logger.info("Default DataSource is [{}]", defaultDS.getConnection().getMetaData().getURL());
        		logger.info("Active DataSource is [{}]",  activeDS.getConnection().getMetaData().getURL());
        	}catch (Exception e) {}
        	*/
        }
    }
    
}