package com.flittly.config;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 让下游服务抛出的异常信息能「干净」地冒出来。
 * <p>
 * 默认情况下 Feign 抛的是 FeignException.InternalServerError，堆栈里只有一大串
 * 请求报文；这里把它转成普通的 RuntimeException，消息取下游返回体里的 message 字段。
 * <p>
 * 注意：这里转出来的仍然是 RuntimeException，所以 @GlobalTransactional 依然会回滚。
 */
@Configuration
public class FeignErrorConfig {

    private static final Pattern MESSAGE = Pattern.compile("\"message\"\\s*:\\s*\"([^\"]*)\"");

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            String body = readBody(response);
            Matcher matcher = MESSAGE.matcher(body);
            String msg = matcher.find() ? matcher.group(1) : body;
            return new RuntimeException("调用 [" + methodKey + "] 失败, status=" + response.status() + ", 原因: " + msg);
        };
    }

    private String readBody(Response response) {
        if (response.body() == null) {
            return "";
        }
        try (InputStream in = response.body().asInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
