package com.flittly.mapper;

import com.flittly.bean.TOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TOrderMapper {

    /** 新增订单，主键回填到 order.id */
    int insert(TOrder order);

    /** 修改订单状态 */
    int updateStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    /** 按主键查询 */
    TOrder selectById(@Param("id") Long id);
}
