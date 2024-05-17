package com.felixprojects.apigateway.config;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

@RequiredArgsConstructor
@Configuration
@Order(5)
@Slf4j
public class BasicAuthenticationFilter implements WebFilter {
@Value("${services.integration.basic.client}")
private String clientSecret;
@Value("${services.integration.basic.password}")
private String clientPassword;
private final Gson gson;
private static final String BASIC = "Basic ";
private static  final String BEARER= "Bearer";
private static final String  INTERNAL_TOKEN_HEADER = "INTERNAL_TOKEN";
private  static  final Predicate<String> matchBasicLength=authValue  ->{
	String [] bearerSplit =  authValue.split (" ");
	if (bearerSplit.length<2) return false;
	return  bearerSplit[0].length ()== BASIC.trim ().length ();
};




@Override
public Mono<Void> filter (ServerWebExchange exchange, WebFilterChain chain) {
	return null;
}
}
