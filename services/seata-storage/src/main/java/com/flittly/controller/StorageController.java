package com.flittly.controller;

import com.flittly.bean.TStorage;
import com.flittly.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/storage")
public class StorageController {

    @Autowired
    private StorageService storageService;

    /** 给 seata-business 调用：库存不足直接抛异常 -> HTTP 500 -> 触发全局回滚 */
    @GetMapping("/deduct")
    public String deduct(@RequestParam("productId") Long productId,
                         @RequestParam("count") Integer count) {
        storageService.deduct(productId, count);
        return "ok";
    }

    /** 观察用：看看回滚之后库存有没有变回来 */
    @GetMapping("/{productId}")
    public TStorage getByProductId(@PathVariable("productId") Long productId) {
        return storageService.getByProductId(productId);
    }
}
