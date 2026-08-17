package com.flittly.order;

import com.flittly.feign.WeatherFeignClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class WeatherTest {

    @Autowired
    WeatherFeignClient weatherFeignClient;

    @Test
    void test01(){
        String weather = weatherFeignClient.getWeather("","1","101010100");
        System.out.println(weather);
    }
}
