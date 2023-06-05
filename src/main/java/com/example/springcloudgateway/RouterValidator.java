package com.example.springcloudgateway;

import org.springframework.http.server.ServerHttpRequest;

import java.util.List;
import java.util.function.Predicate;

public class RouterValidator {
    public static final List<String>openApiEndpoints = List.of(
            "/auth/register",
            "auth/token",
            "/eureka"
    );
    public Predicate<ServerHttpRequest>isSecured =
           request ->openApiEndpoints
                   . stream()
                   .noneMatch(uri -> request.getURI().getPath().contains(uri));

}
