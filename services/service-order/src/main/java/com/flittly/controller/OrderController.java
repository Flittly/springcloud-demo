package com.flittly.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.flittly.bean.Order;
import com.flittly.properties.OrderProperties;
import com.flittly.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//@RequestMapping("/api/order")
@Slf4j
//@RefreshScope
@RestController
public class OrderController {

    @Autowired
    OrderService orderService;

//    @Value("${order.timeout}")
//    String orderTimeout;
//    @Value("${order.auto-confirm}")
//    String orderAutoConfirm;

    @Autowired
    OrderProperties orderProperties;

    @GetMapping("/config")
    public String config(){
        return "order timeout: " + orderProperties.getTimeout() + ": " +
                "order auto-confirm: " + orderProperties.getAutoConfirm() + ": " +
                "order db-url: " + orderProperties.getDbUrl();
    }

    @GetMapping("/create")
    public Order creatOrder(@RequestParam("userId") Long userId,
                            @RequestParam("productId") Long productId){
        Order order = orderService.createOrder(productId, userId);
        return order;
    }

    @GetMapping("/seckill")
    @SentinelResource(value = "seckill-order", fallback = "seckillFallback")
    public Order seckillOrder(@RequestParam("userId") Long userId,
                            @RequestParam("productId") Long productId){
        Order order = orderService.createOrder(productId, userId);
        order.setId(Long.MAX_VALUE);
        return order;
    }

    public Order seckillFallback(Long userId, Long productId, BlockException exception){
        System.out.println("seckillFallback....");
        Order order = new Order();
        order.setId(productId);
        order.setUserId(userId);
        order.setAddress("异常信息：" + exception.getClass());
        return order;
    }

    @GetMapping("/writeDb")
    public String writeDb(){
        log.info("write db");
        return "write db";
    }

    @GetMapping("/readDb")
    public String readDb(){
        log.info("read db");
        return "read db";
    }
}
