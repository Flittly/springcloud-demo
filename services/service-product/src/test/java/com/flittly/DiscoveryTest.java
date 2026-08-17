package com.flittly;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

@SpringBootTest
public class DiscoveryTest {
    @Autowired
    DiscoveryClient discoveryClient;

    @Test
    void discoveryClientTest(){
        for (String service : discoveryClient.getServices()) {
            System.out.println("service=" + service);
            //获取ip+port
            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            for(ServiceInstance instance : instances){
                System.out.println("instance=" + instance.getHost() + ":" + instance.getPort());
            }
        }
    }
}
