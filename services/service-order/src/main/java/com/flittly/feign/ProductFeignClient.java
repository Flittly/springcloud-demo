package com.flittly.feign;

import com.flittly.bean.Product;
import com.flittly.feign.fallback.ProductFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "service-product", fallback = ProductFeignClientFallback.class) // feign客户端
public interface  ProductFeignClient {
    // mvc注解的两套使用逻辑
    // 1、标注在Controller上，是接受这样的请求。
    // 2、标注在FeignClient上，是发送这样的请求。
    @GetMapping("/product/{id}")
    Product getProductById(@PathVariable("id") Long id); // 逻辑和controller里的相反，这里的是将数据给远程服务
}
