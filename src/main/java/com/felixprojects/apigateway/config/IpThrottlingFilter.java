package com.felixprojects.apigateway.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.bucket4j.*;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.cache.Cache;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class IpThrottlingFilter implements GlobalFilter {
    //cache for storing token buckets
    private final Cache<String, GridBucketState> cache;
    private final ProxyManager<String> buckets;

    public IpThrottlingFilter(Cache<String, GridBucketState> cache) {
        this.cache = cache;
        //init bucket registry
        buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(this.cache);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        BucketConfiguration bucketConfiguration = Bucket4j.configurationBuilder()
                .addLimit(Bandwidth.simple(10, Duration.ofSeconds (1))) // sets a bucket of 20 tokens for a duration of 1 minute
                .build();
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //Configuration that will be called on first interaction with proxy if bucket  was not saved previously
        Supplier<BucketConfiguration> configurationLazySupplier = () -> bucketConfiguration;

        //acquire cheap proxy to bucket
        Bucket bucket = buckets.getProxy(request.getRemoteAddress().getHostString(), configurationLazySupplier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            ServerHttpResponse decoratedResponse = new ServerHttpResponseDecorator (response) {
                @Override
                public HttpHeaders getHeaders() {
                    HttpHeaders headers = new HttpHeaders();
                    headers.addAll(super.getHeaders());
                    headers.add ("X-Rate-Limit-Remaining", String.valueOf (probe.getRemainingTokens()));
                    return headers;
                }
            };
            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        } else {
            Gson gson = new Gson();
            Map<String, String> responseData = new LinkedHashMap<>();
            responseData.put("error", "too many requests");
            responseData.put("error description", "please wait");
            Type gsonType = new TypeToken<LinkedHashMap<String, String>>() {
            }.getType();
            String gsonString = gson.toJson(responseData, gsonType);
            DataBuffer bodyDataBuffer = response.bufferFactory().wrap(gsonString.getBytes());
            exchange.getResponse ().setStatusCode (HttpStatus.OK);
            return exchange.getResponse ().writeWith (Mono.just (bodyDataBuffer))
                    .flatMap (res-> exchange.getResponse ().setComplete ());
        }
    }

}
