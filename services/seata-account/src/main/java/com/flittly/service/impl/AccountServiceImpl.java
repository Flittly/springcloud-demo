package com.flittly.service.impl;

import com.flittly.bean.TAccount;
import com.flittly.mapper.TAccountMapper;
import com.flittly.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private TAccountMapper accountMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debit(Long userId, BigDecimal money) {
        TAccount account = accountMapper.selectByUserId(userId);
        if (account == null) {
            throw new RuntimeException("账户不存在, userId=" + userId);
        }
        if (account.getMoney().compareTo(money) < 0) {
            throw new RuntimeException("余额不足, 当前余额=" + account.getMoney() + ", 本次需要=" + money);
        }
        accountMapper.debit(userId, money);
        log.info("[seata_account] 本地事务提交, 用户 {} 扣减 {} 元, XID={}",
                userId, money, RootContext.getXID());
    }

    @Override
    public TAccount getByUserId(Long userId) {
        return accountMapper.selectByUserId(userId);
    }
}
