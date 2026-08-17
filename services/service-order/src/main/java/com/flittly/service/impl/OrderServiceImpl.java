package com.flittly.service.impl;

import com.flittly.bean.Order;
import com.flittly.bean.Product;
import com.flittly.feign.ProductFeignClient;
import com.flittly.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    DiscoveryClient discoveryClient;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @Override
    public Order createOrder(Long productId, Long userId) {
        // Product product = getProductFromRemoteWithAnnotation(productId);
        Product product = productFeignClient.getProductById(productId);
        Order order = new Order();

        order.setId(1L);
        // 总金额
        order.setTotalAmount(product.getPrice().multiply(new BigDecimal(product.getNum())));
        order.setUserId(userId);
        order.setNickName("testName");
        order.setAddress("testAddress");
        order.setProductList(Arrays.asList(product));

        return order;
    }

    private Product getProductFromRemote(Long productId){
        // 1、调用远程服务获取商品信息
        List<ServiceInstance> instances = discoveryClient.getInstances("service-product");

        ServiceInstance instance = instances.get(0);
        String url = "http://" + instance.getHost() + ":" + instance.getPort() + "/product/" + productId;
        // 2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        log.info("远程请求：{}", url);

        return product;
    }

    // 进阶2：完成负载均衡发送请求
    private Product getProductFromRemotewithLoadBalance(Long productId){
        // 1、调用远程服务获取商品信息
        ServiceInstance choose = loadBalancerClient.choose("service-product");

        String url = "http://" + choose.getHost() + ":" + choose.getPort() + "/product/" + productId;
        // 2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        log.info("远程请求：{}", url);

        return product;
    }

    // 进阶3：基于注解的负载均衡
    private Product getProductFromRemoteWithAnnotation(Long productId){
        // 1、调用远程服务获取商品信息
        String url = "http://service-product/product/" + productId; // service-product会被动态替换
        // 2、给远程发送请求
        Product product = restTemplate.getForObject(url, Product.class);
        log.info("远程请求：{}", url);

        return product;
    }
}
