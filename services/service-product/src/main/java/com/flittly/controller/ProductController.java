package com.flittly.controller;

import com.flittly.bean.Product;
import com.flittly.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

@RequestMapping("/api/product")
@RestController
public class ProductController {

    @Autowired
    ProductService productService;

    // 查询商品
    @GetMapping("/product/{id}")
    public Product getProduct(@PathVariable("id") Long productId, HttpServletRequest request)  {

        String header = request.getHeader("X-Token");
        System.out.println("查询商品：" + productId + "hello ... token" + header);
        Product product = productService.getProductById(productId);
//        try{
//            TimeUnit.SECONDS.sleep(2);
//        }catch (InterruptedException e){
//            throw new RuntimeException(e);
//        }
        return product;
    }
}
