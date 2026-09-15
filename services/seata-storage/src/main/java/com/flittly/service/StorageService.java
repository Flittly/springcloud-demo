package com.flittly.service;

import com.flittly.bean.TStorage;

public interface StorageService {

    /** 扣减库存，库存不足抛异常 */
    void deduct(Long productId, Integer count);

    /** 查询库存 */
    TStorage getByProductId(Long productId);
}
