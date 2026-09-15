package com.flittly.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

/**
 * 账户服务
 */
@FeignClient(name = "seata-account")
public interface AccountFeignClient {

    /**
     * 扣减余额，余额不足时下游会抛异常 -> HTTP 500 -> 这里抛出 -> 全局事务回滚
     */
    @GetMapping("/account/debit")
    String debit(@RequestParam("userId") Long userId,
                 @RequestParam("money") BigDecimal money);
}
