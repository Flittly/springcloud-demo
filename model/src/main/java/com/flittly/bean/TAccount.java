package com.flittly.bean;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户表 t_account 对应实体（Seata 分布式事务示例）
 */
@Data
public class TAccount {
    private Long id;
    /** 用户 id */
    private Long userId;
    /** 账户余额 */
    private BigDecimal money;
}
