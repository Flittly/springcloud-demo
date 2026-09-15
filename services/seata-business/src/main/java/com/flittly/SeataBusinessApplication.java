package com.flittly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Seata 全局事务发起方（TM：Transaction Manager）
 * <p>
 * 自身不连数据库，只负责按顺序调用 order / storage / account 三个服务，
 * 并用 @GlobalTransactional 把它们各自的本地事务合并成一个全局事务。
 */
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class SeataBusinessApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeataBusinessApplication.class, args);
    }
}
