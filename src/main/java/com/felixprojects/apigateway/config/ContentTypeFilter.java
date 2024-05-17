package com.felixprojects.apigateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@Order (8)
public class ContentTypeFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpResponse response = exchange.getResponse();
        HttpHeaders headers = response.getHeaders();
        // Check if the content type is already set
        if (headers.getContentType() == null) {
            // Set the content type to JSON
          // Get the path the request was sent to
            String path = exchange.getRequest().getPath().toString();
            if (!path.contains("/product/download")) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
        }
        return chain.filter(exchange);
    }}