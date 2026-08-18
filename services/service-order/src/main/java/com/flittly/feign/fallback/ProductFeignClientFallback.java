package com.flittly.feign.fallback;

import com.flittly.bean.Product;
import com.flittly.feign.ProductFeignClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductFeignClientFallback implements ProductFeignClient {
    @Override
    public Product getProductById(Long id) {
        System.out.println("兜底回调......");

        Product product = new Product();
        product.setId(id);
        product.setPrice(new BigDecimal("0.00"));
        product.setProductName("未知商品-兜底回调");
        product.setNum(0);

        return product;
    }
}
