package com.flittly.controller;

import com.flittly.bean.TOrder;
import com.flittly.common.R;
import com.flittly.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    /**
     * 下单（全局事务入口）
     * <p>
     * 成功用例：/business/purchase?userId=1&productId=1&count=1&money=100
     * 回滚用例：/business/purchase?userId=1&productId=1&count=10&money=10000
     * （余额只有 1000，第三步必炸，前两步会被 Seata 回滚）
     */
    @GetMapping("/purchase")
    public R purchase(@RequestParam("userId") Long userId,
                      @RequestParam("productId") Long productId,
                      @RequestParam("count") Integer count,
                      @RequestParam("money") BigDecimal money) {
        TOrder order = businessService.purchase(userId, productId, count, money);
        return R.ok(order);
    }
}
