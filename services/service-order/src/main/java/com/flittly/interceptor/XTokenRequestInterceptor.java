package com.flittly.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class XTokenRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        System.out.println("XTokenRequestInterceptor......");
        template.header("X-Token", UUID.randomUUID().toString());
    }
}
