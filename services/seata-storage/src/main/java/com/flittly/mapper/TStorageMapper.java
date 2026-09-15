package com.flittly.mapper;

import com.flittly.bean.TStorage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TStorageMapper {

    /** 按商品 id 查询库存 */
    TStorage selectByProductId(@Param("productId") Long productId);

    /** 扣减库存 */
    int deduct(@Param("productId") Long productId, @Param("count") Integer count);
}
