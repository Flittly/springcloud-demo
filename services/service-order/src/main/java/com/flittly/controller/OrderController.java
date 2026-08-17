package com.flittly.controller;

import com.flittly.bean.Order;
import com.flittly.properties.OrderProperties;
import com.flittly.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
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
}
