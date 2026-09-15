package com.flittly.bean;

import lombok.Data;

/**
 * 库存表 t_storage 对应实体（Seata 分布式事务示例）
 */
@Data
public class TStorage {
    private Long id;
    /** 商品 id */
    private Long productId;
    /** 剩余库存 */
    private Integer count;
}
