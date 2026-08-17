package com.flittly.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "service-weather", url = "http://aliv18.data.moji.com")
public interface WeatherFeignClient {
    @PostMapping("/whapi/json/alicityweather/condition")
    String getWeather(@RequestHeader("Authorization") String auth,
                      @RequestParam("token")String token,
                      @RequestParam("cityId") String cityId);

}
