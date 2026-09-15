package com.flittly.service;

import com.flittly.bean.TOrder;

import java.math.BigDecimal;

public interface OrderService {

    /** 创建订单 */
    TOrder create(Long userId, Long productId, Integer count, BigDecimal money);

    /** 订单完结 */
    void finish(Long orderId);

    /** 查询订单 */
    TOrder getById(Long id);
}
