package com.flittly.mapper;

import com.flittly.bean.TAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface TAccountMapper {

    /** 按用户 id 查询账户 */
    TAccount selectByUserId(@Param("userId") Long userId);

    /** 扣减余额 */
    int debit(@Param("userId") Long userId, @Param("money") BigDecimal money);
}
