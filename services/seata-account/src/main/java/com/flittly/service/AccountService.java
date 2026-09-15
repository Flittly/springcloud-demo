package com.flittly.service;

import com.flittly.bean.TAccount;

import java.math.BigDecimal;

public interface AccountService {

    /** 扣减余额，余额不足抛异常 */
    void debit(Long userId, BigDecimal money);

    /** 查询账户 */
    TAccount getByUserId(Long userId);
}
