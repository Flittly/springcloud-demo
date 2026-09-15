-- ============================================================
-- Seata Server 自身的存储库（不是业务库！）
--
-- 什么时候需要执行这个脚本？
--   * 只有把 seata-server 的 store.mode 配成 db 时才需要（本项目 docker-compose 默认就是 db）
--   * 用 seata-server 默认的 store.mode=file 时，这个库可以不建
--
-- 业务库只有 3 个：seata_account / seata_storage / seata_order
-- 这个库纯粹是 Seata Server 用来记录「全局事务 / 分支事务 / 全局锁」的
--
-- 执行：mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 04-seata-server-db.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS `seata`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `seata`;

-- 全局事务会话信息
CREATE TABLE IF NOT EXISTS `global_table`
(
    `xid`                       VARCHAR(128) NOT NULL,
    `transaction_id`            BIGINT,
    `status`                    TINYINT      NOT NULL,
    `application_id`            VARCHAR(32),
    `transaction_service_group` VARCHAR(32),
    `transaction_name`          VARCHAR(128),
    `timeout`                   INT,
    `begin_time`                BIGINT,
    `application_data`          VARCHAR(2000),
    `gmt_create`                DATETIME,
    `gmt_modified`              DATETIME,
    PRIMARY KEY (`xid`),
    KEY `idx_status_gmt_modified` (`status`, `gmt_modified`),
    KEY `idx_transaction_id` (`transaction_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 分支事务会话信息
CREATE TABLE IF NOT EXISTS `branch_table`
(
    `branch_id`         BIGINT       NOT NULL,
    `xid`               VARCHAR(128) NOT NULL,
    `transaction_id`    BIGINT,
    `resource_group_id` VARCHAR(32),
    `resource_id`       VARCHAR(256),
    `branch_type`       VARCHAR(8),
    `status`            TINYINT,
    `client_id`         VARCHAR(64),
    `application_data`  VARCHAR(2000),
    `gmt_create`        DATETIME(6),
    `gmt_modified`      DATETIME(6),
    PRIMARY KEY (`branch_id`),
    KEY `idx_xid` (`xid`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 全局锁
CREATE TABLE IF NOT EXISTS `lock_table`
(
    `row_key`        VARCHAR(128) NOT NULL,
    `xid`            VARCHAR(128),
    `transaction_id` BIGINT,
    `branch_id`      BIGINT       NOT NULL,
    `resource_id`    VARCHAR(256),
    `table_name`     VARCHAR(32),
    `pk`             VARCHAR(36),
    `gmt_create`     DATETIME,
    `gmt_modified`   DATETIME,
    PRIMARY KEY (`row_key`),
    KEY `idx_branch_id` (`branch_id`),
    KEY `idx_xid_and_branch_id` (`xid`, `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- Server 集群/定时任务用的分布式锁
CREATE TABLE IF NOT EXISTS `distributed_lock`
(
    `lock_key`   CHAR(20)    NOT NULL,
    `lock_value` VARCHAR(20) NOT NULL,
    `expire`     BIGINT,
    PRIMARY KEY (`lock_key`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('AsyncCommitting', ' ', 0)
ON DUPLICATE KEY UPDATE lock_key = lock_key;
INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('RetryCommitting', ' ', 0)
ON DUPLICATE KEY UPDATE lock_key = lock_key;
INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('RetryRollbacking', ' ', 0)
ON DUPLICATE KEY UPDATE lock_key = lock_key;
INSERT INTO `distributed_lock` (lock_key, lock_value, expire)
VALUES ('TxTimeoutCheck', ' ', 0)
ON DUPLICATE KEY UPDATE lock_key = lock_key;
