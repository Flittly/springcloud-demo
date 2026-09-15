-- ============================================================
-- Seata 分布式事务示例 —— 订单服务数据库 seata_order
-- MySQL 8.0 / InnoDB / utf8mb4
-- 执行：mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 03-seata-order.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `seata_order`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `seata_order`;

-- ------------------------------------------------------------
-- 业务表：订单
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order`
(
    `id`         BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT         NOT NULL COMMENT '用户 id',
    `product_id` BIGINT         NOT NULL COMMENT '商品 id',
    `count`      INT            NOT NULL DEFAULT 0 COMMENT '购买数量',
    `money`      DECIMAL(11, 0) NOT NULL DEFAULT 0 COMMENT '订单金额',
    `status`     INT            NOT NULL DEFAULT 0 COMMENT '0-创建中，1-已完结',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='订单表';

-- ------------------------------------------------------------
-- Seata AT 模式回滚日志表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`
(
    `branch_id`     BIGINT       NOT NULL COMMENT '分支事务 id',
    `xid`           VARCHAR(128) NOT NULL COMMENT '全局事务 id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log 上下文，如序列化方式',
    `rollback_info` LONGBLOB     NOT NULL COMMENT '回滚信息',
    `log_status`    INT          NOT NULL COMMENT '0-正常，1-防悬挂',
    `log_created`   DATETIME(6)  NOT NULL COMMENT '创建时间',
    `log_modified`  DATETIME(6)  NOT NULL COMMENT '修改时间',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AT 模式回滚日志表';
