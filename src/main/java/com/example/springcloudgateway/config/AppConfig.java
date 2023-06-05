package com.example.springcloudgateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

public class AppConfig {
    @Bean
    public WebClient webClient(){
        return WebClient.create();
    }
}
