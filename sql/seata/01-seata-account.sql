-- ============================================================
-- Seata 分布式事务示例 —— 账户服务数据库 seata_account
-- MySQL 8.0 / InnoDB / utf8mb4
-- 执行：mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 01-seata-account.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `seata_account`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `seata_account`;

-- ------------------------------------------------------------
-- 业务表：账户
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `t_account`;
CREATE TABLE `t_account`
(
    `id`      BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT         NOT NULL COMMENT '用户 id',
    `money`   DECIMAL(11, 0) NOT NULL DEFAULT 0 COMMENT '账户余额',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='账户表';

-- 初始数据：用户 1 余额 1000
INSERT INTO `t_account` (`id`, `user_id`, `money`)
VALUES (1, 1, 1000);

-- ------------------------------------------------------------
-- Seata AT 模式回滚日志表（每个参与事务的库都必须有，表名固定为 undo_log）
-- 由 Seata 自动写入，不要手工修改
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
