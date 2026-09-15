package com.flittly.controller;

import com.flittly.bean.TAccount;
import com.flittly.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /** 给 seata-business 调用：余额不足直接抛异常 -> HTTP 500 -> 触发全局回滚 */
    @GetMapping("/debit")
    public String debit(@RequestParam("userId") Long userId,
                        @RequestParam("money") BigDecimal money) {
        accountService.debit(userId, money);
        return "ok";
    }

    /** 观察用：看看回滚之后余额有没有变回来 */
    @GetMapping("/{userId}")
    public TAccount getByUserId(@PathVariable("userId") Long userId) {
        return accountService.getByUserId(userId);
    }
}
