package com.flittly.service;

import com.flittly.bean.TOrder;
import com.flittly.feign.AccountFeignClient;
import com.flittly.feign.OrderFeignClient;
import com.flittly.feign.StorageFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.apache.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 下单主流程。
 * <p>
 * 这里是最关键的类：一个方法里跨了 3 个服务、3 个数据库，
 * 只靠 {@link GlobalTransactional} 一个注解把它们绑成一个全局事务。
 */
@Slf4j
@Service
public class BusinessService {

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private StorageFeignClient storageFeignClient;

    @Autowired
    private AccountFeignClient accountFeignClient;

    /**
     * 下单：创建订单 -> 扣库存 -> 扣余额 -> 订单完结
     * <p>
     * 方法结束时：
     * - 正常返回  -> TM 通知所有分支「提交」（各库删掉 undo_log）
     * - 抛异常    -> TM 通知所有分支「回滚」（各库用 undo_log 反向补偿）
     *
     * @param money 订单金额，故意做成入参，方便你传个大数触发「余额不足」看回滚
     */
    @GlobalTransactional(name = "seata-business-purchase", rollbackFor = Exception.class)
    public TOrder purchase(Long userId, Long productId, Integer count, BigDecimal money) {
        log.info("========== 全局事务开始: XID = {} ==========", RootContext.getXID());

        // 1. seata-order 库：插入订单，status = 0（创建中）
        TOrder order = orderFeignClient.create(userId, productId, count, money);
        log.info("1. 创建订单成功, orderId = {}", order.getId());

        // 2. seata-storage 库：扣减库存
        storageFeignClient.deduct(productId, count);
        log.info("2. 扣减库存成功");

        // 3. seata-account 库：扣减余额（余额不足会在这里炸，前两步会被回滚）
        accountFeignClient.debit(userId, money);
        log.info("3. 扣减余额成功");

        // 4. seata-order 库：把订单置为已完结
        orderFeignClient.finish(order.getId());
        log.info("4. 订单完结");

        order.setStatus(1);
        log.info("========== 全局事务提交 ==========");
        return order;
    }
}
