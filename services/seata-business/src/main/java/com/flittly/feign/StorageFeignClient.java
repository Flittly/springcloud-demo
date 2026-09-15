package com.flittly.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 库存服务
 */
@FeignClient(name = "seata-storage")
public interface StorageFeignClient {

    /**
     * 扣减库存，库存不足时下游会抛异常 -> HTTP 500 -> 这里抛出 -> 全局事务回滚
     */
    @GetMapping("/storage/deduct")
    String deduct(@RequestParam("productId") Long productId,
                  @RequestParam("count") Integer count);
}
