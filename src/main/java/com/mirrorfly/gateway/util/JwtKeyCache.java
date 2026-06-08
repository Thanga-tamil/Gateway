package com.mirrorfly.gateway.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@Component
public class JwtKeyCache {

    @Autowired
    private ApplicationProperties applicationProperties;

    @Getter
    private byte[] secret;

    public JwtKeyCache(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    public void init() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        String jwtKeyPath = applicationProperties.getJwtKeyPath();
        String jwkContent = Files.readString(Paths.get(jwtKeyPath));

        JsonNode jwk = mapper.readTree(jwkContent);
        String k = jwk.get("k").asText();
        
        System.out.println("Jwt secret key loaded into in_memory");
        this.secret = Base64.getUrlDecoder().decode(k);
    }

}