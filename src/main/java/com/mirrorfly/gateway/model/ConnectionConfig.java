package com.mirrorfly.gateway.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "connection_config")
public class ConnectionConfig implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "customer_domain")
    private String customerDomain;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "connection_string")
    private String connectionString;
    
    @Column(name = "db_username")
    private String dbUsername;
    
    @Column(name = "db_password")
    private String dbPassword;
    
    @Column(name = "minimum_connections")
    private int minimumConnections;
    
    @Column(name = "maximum_connections")
    private int maximumConnections;
    
    
}
