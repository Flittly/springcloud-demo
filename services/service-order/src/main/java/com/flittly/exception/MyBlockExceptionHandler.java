package com.flittly.exception;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flittly.common.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;

@Component
public class MyBlockExceptionHandler implements BlockExceptionHandler {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, String resourceName,BlockException e) throws Exception {
        // 自定义的限流处理逻辑
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(429);
        PrintWriter writer = response.getWriter();

        R error = R.error(500, resourceName + "被sentinel限制了，原因：" + e.getClass());

        String json = objectMapper.writeValueAsString(error);
        writer.write(json);
        writer.flush(); // 刷新流
        writer.close(); // 关闭流
    }
}
