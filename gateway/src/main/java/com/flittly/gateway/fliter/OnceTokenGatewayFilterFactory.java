package com.flittly.gateway.fliter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class OnceTokenGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {
    @Override
    public GatewayFilter apply(NameValueConfig config) {
        return new GatewayFilter() {
            @Override
            public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
                // 每次响应之前，添加一个一次性令牌，支持uuid，jwt等令牌
                return chain.filter(exchange).then(Mono.fromRunnable(()->{
                    ServerHttpResponse response = exchange.getResponse();
                    HttpHeaders headers = response.getHeaders();
                    String value = config.getValue();
                    if("uuid".equalsIgnoreCase(value)){
                        value = java.util.UUID.randomUUID().toString();
                    }
                    if("jwt".equalsIgnoreCase(value)){
                        value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMDA4NiIsIm5hbWUiOiJhZG1pbiIsInJvbGUiOiJVU0VSIiwiaXNzIjoic2Vja2lsbC1nYXRld2F5IiwiaWF0IjoxNzU3OTEyNjIwLCJleHAiOjE3NTc5MTk4MjB9.Xk9mT2vR4sLqW8nJ6pA3bF7cD1eH5gI0jK2lM4oN6qS";
                    }
                    headers.add(config.getName(), value);
                }));
            }
        };
    }
}
