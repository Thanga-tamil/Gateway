package com.mirrorfly.gateway.model;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.Data;

@Entity
@Table(name = "email_token")
@Data
public class EmailToken {
     
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "token")
    private String token;
    
    @Column(name = "email_template")
    private String emailTemplate;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "expired_date_time")
    private LocalDateTime expiredDateTime;
    
    @Column(name = "issued_date_time")
    private LocalDateTime issuedDateTime;
    
    @Column(name = "confirmed_date_time")
    private LocalDateTime confirmedDateTime;
    
    @Column(name = "email_id")
    private String emailId;
    
    public EmailToken(){
        this.token = UUID.randomUUID().toString();
        this.issuedDateTime = LocalDateTime.now();
        this.expiredDateTime = this.issuedDateTime.plusDays(1);
        this.status = GatewayConstants.STATUS_PENDING;
    }

}
