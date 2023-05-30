package com.mirrorfly.gateway.service;

import java.util.List;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ConnectionConfigRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.Response;

import reactor.core.publisher.Mono;

@Service
public class AdminService {

	@Autowired
	private ConnectionConfigRepo connectionConfigRepo;

	@Autowired
	private DataStore dataStore;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private ApplicationProperties applicationProperties;
	
    private WebClient webClient;
    
    @Autowired
    private UserRepo userRepo;
    
    public AdminService(WebClient.Builder webClientBuilder, ApplicationProperties applicationProperties) {
    	this.webClient = webClientBuilder.baseUrl(applicationProperties.getSandboxUrl()).build();
    }

	public Response<String> reloadConnectionConfig() {
		Response<String> response = new Response<>();
		try {
			List<ConnectionConfig> connectionConfigs = connectionConfigRepo.findAll();
			dataStore.setConnectionConfig(connectionConfigs);

			AbstractRoutingDataSourceImpl inMemoryDataSources = new AbstractRoutingDataSourceImpl();
			AutowireCapableBeanFactory autowireCapableBeanFactory = applicationContext.getAutowireCapableBeanFactory();
			autowireCapableBeanFactory.autowireBean(inMemoryDataSources);

			inMemoryDataSources.refresh(connectionConfigs);

			response.setStatus(HttpStatus.SC_OK);
			response.setMessage("Gateway service inmemory data reloaded successfully.");
		} catch (Exception e) {
			response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.setMessage("Could not process your request. Try again later.");
		}
		return response;
	}
	
	@SwitchDataSource(value="[0]")
	public boolean isAdmin(String userdomain, String username) {
		
		if(!applicationProperties.getSandboxUrl().startsWith(userdomain)) {
			return false;
		}
		
		Users user = userRepo.findByUsername(username);
		boolean result = false;
		if(user != null && user.getIsAdmin() == 1) {
			result = true;
		}
		return result;
	}
	
    public Mono<Response> reloadAll(String uri, String token) {
    	return this.webClient.get()
    		     .uri(uri)
    		     .accept(MediaType.APPLICATION_JSON)
    		     .header("Authorization", token)
    		     .exchangeToMono(response -> {
    		         if (response.statusCode().is2xxSuccessful()) {
    		             return response.bodyToMono(Response.class);
    		         }
    		         else {
    		             return response.createException().flatMap(Mono::error);
    		         }
    		     });
    }

}
