package com.flittly.service;

import com.flittly.bean.Order;

public interface OrderService {
    Order createOrder(Long productId, Long userId);
}
