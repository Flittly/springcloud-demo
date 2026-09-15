package com.flittly.service.impl;

import com.flittly.bean.TOrder;
import com.flittly.mapper.TOrderMapper;
import com.flittly.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TOrderMapper orderMapper;

    /**
     * @Transactional 管的是「本地事务」（seata_order 这一个库）。
     * Seata 会把它当成全局事务下的一个分支事务；
     * 每个分支执行前，Seata 会往 undo_log 写一份「前镜像」，以便全局回滚时反向补偿。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TOrder create(Long userId, Long productId, Integer count, BigDecimal money) {
        TOrder order = new TOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setCount(count);
        order.setMoney(money);
        order.setStatus(0); // 0-创建中
        orderMapper.insert(order);
        log.info("[seata_order] 本地事务提交, 插入订单 id={}, XID={}", order.getId(), RootContext.getXID());
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(Long orderId) {
        orderMapper.updateStatus(orderId, 1);
        log.info("[seata_order] 本地事务提交, 订单 {} 置为已完结, XID={}", orderId, RootContext.getXID());
    }

    @Override
    public TOrder getById(Long id) {
        return orderMapper.selectById(id);
    }
}
