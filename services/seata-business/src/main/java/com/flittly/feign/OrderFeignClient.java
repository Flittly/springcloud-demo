package com.flittly.feign;

import com.flittly.bean.TOrder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 订单服务
 */
@FeignClient(name = "seata-order")
public interface OrderFeignClient {

    /**
     * 创建订单（状态 0-创建中）
     */
    @GetMapping("/order/create")
    TOrder create(@RequestParam("userId") Long userId,
                  @RequestParam("productId") Long productId,
                  @RequestParam("count") Integer count,
                  @RequestParam("money") BigDecimal money);

    /**
     * 订单完结（状态 1-已完结）
     */
    @GetMapping("/order/finish")
    String finish(@RequestParam("orderId") Long orderId);
}
