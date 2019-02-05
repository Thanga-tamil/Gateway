package com.mirrorfly.gateway.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.service.TokenValidation;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

@Component
public class GatewayZuulFilter extends ZuulFilter {

	@Autowired
	private TokenValidation tokenValidation;

	@Override
	public String filterType() {
		return "pre";
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	/**
	 * This object method run is used to handle zuul token validations
	 */
	@Override
	public Object run() {
		RequestContext ctx = RequestContext.getCurrentContext();
		if (ctx.getRequest().getMethod().equals(GatewayConstants.OPTIONS)) {
			ctx.setResponseStatusCode(HttpServletResponse.SC_OK);
			ctx.setResponseBody(GatewayConstants.OK);
			ctx.setSendZuulResponse(false);
		} else {
			HttpServletRequest request = ctx.getRequest();
			String header = request.getHeader(GatewayConstants.AUTHORIZATION);
			if (header == null || header.isEmpty()) {
				sendTokenExpiredResponse(ctx);
			} else {
				String username = tokenValidation.validateToken(header);
				if (username == null) {
					sendTokenExpiredResponse(ctx);
				} else {
					HttpServletRequestWrapper wrappedRequest = modifyRequest(request);
					ctx.setRequest(wrappedRequest);
					ctx.addZuulRequestHeader(GatewayConstants.USER_NAME_HEADER, username);
				}
			}
		}
		return null;
	}

	/**
	 * This Method is used to set Content Type JSON.
	 * 
	 * @param request http request
	 * @return success / error
	 */
	private HttpServletRequestWrapper modifyRequest(HttpServletRequest request) {
		return new HttpServletRequestWrapper(request) {

			@Override
			public String getContentType() {
				return GatewayConstants.APP_JSON;
			}

		};
	}

	/**
	 * This Method is used to throw the exception message like Token Expired to
	 * alert the user
	 * 
	 * @param ctx request context
	 */
	private void sendTokenExpiredResponse(RequestContext ctx) {
		try {
			ctx.setResponseStatusCode(HttpServletResponse.SC_UNAUTHORIZED);
			String jsonO = GatewayConstants.TOKEN_EXPIRED_JSON;
			ObjectMapper mapper = new ObjectMapper();
			JsonNode actualObj = mapper.readTree(jsonO);
			String gsonObject = actualObj.toString();
			ctx.getResponse().getWriter().print(gsonObject);
			ctx.getResponse().flushBuffer();
		} catch (IOException e) {
			ctx.setResponseBody(GatewayConstants.TOKEN_EXPIRED);
			ctx.setSendZuulResponse(false);
		}
		ctx.setSendZuulResponse(false);
	}

}
