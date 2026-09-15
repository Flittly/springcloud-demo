package com.flittly.controller;

import com.flittly.bean.TOrder;
import com.flittly.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 这些接口主要给 seata-business 通过 Feign 调用。
 * 成功返回正常结果，失败直接抛异常（HTTP 500），
 * 这样业务方 Feign 就会抛异常，从而触发全局回滚。
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/create")
    public TOrder create(@RequestParam("userId") Long userId,
                         @RequestParam("productId") Long productId,
                         @RequestParam("count") Integer count,
                         @RequestParam("money") BigDecimal money) {
        return orderService.create(userId, productId, count, money);
    }

    @GetMapping("/finish")
    public String finish(@RequestParam("orderId") Long orderId) {
        orderService.finish(orderId);
        return "ok";
    }

    @GetMapping("/{id}")
    public TOrder getById(@PathVariable("id") Long id) {
        return orderService.getById(id);
    }
}
