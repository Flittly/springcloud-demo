package com.flittly.service.impl;

import com.flittly.bean.TStorage;
import com.flittly.mapper.TStorageMapper;
import com.flittly.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.seata.core.context.RootContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class StorageServiceImpl implements StorageService {

    @Autowired
    private TStorageMapper storageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long productId, Integer count) {
        TStorage storage = storageMapper.selectByProductId(productId);
        if (storage == null) {
            throw new RuntimeException("商品不存在, productId=" + productId);
        }
        if (storage.getCount() < count) {
            throw new RuntimeException("库存不足, 当前库存=" + storage.getCount() + ", 本次需要=" + count);
        }
        storageMapper.deduct(productId, count);
        log.info("[seata_storage] 本地事务提交, 商品 {} 扣减 {} 件, XID={}",
                productId, count, RootContext.getXID());
    }

    @Override
    public TStorage getByProductId(Long productId) {
        return storageMapper.selectByProductId(productId);
    }
}
