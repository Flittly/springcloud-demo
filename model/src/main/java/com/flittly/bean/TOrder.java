package com.flittly.bean;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单表 t_order 对应实体（Seata 分布式事务示例）
 */
@Data
public class TOrder {
    private Long id;
    /** 用户 id */
    private Long userId;
    /** 商品 id */
    private Long productId;
    /** 购买数量 */
    private Integer count;
    /** 订单金额 */
    private BigDecimal money;
    /** 订单状态：0-创建中，1-已完结 */
    private Integer status;
}
