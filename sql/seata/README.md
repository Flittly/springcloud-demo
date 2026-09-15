# Seata 分布式事务示例（AT 模式）

一个「下单」业务，跨 3 个服务、3 个数据库，用 Seata AT 模式保证整体一致性。

```
                     ┌──────────────────────────┐
   HTTP  /business/purchase                  │
        │            │  seata-business  :9101   │  ← TM，@GlobalTransactional
        │            └────────────┬─────────────┘     自己不连库
        │                         │ Feign
        │      ┌──────────────────┼──────────────────┐
        │      ▼                  ▼                  ▼
        │  seata-order       seata-storage      seata-account
        │     :9102              :9103              :9104
        │  seata_order        seata_storage      seata_account
        │   (t_order)         (t_storage)        (t_account)
        │      └──────────────────┴──────────────────┘
        │                         │
        └─────────────────►  Seata Server :8091 / :7091
                            （客户端直连 8091；事务会话在容器内本地文件）
```

## 一、模块与端口

| 模块 | 端口 | 角色 | 数据库 |
|---|---|---|---|
| `seata-business` | 9101 | TM（全局事务发起方） | 无，只做服务编排 |
| `seata-order` | 9102 | RM | `seata_order` |
| `seata-storage` | 9103 | RM | `seata_storage` |
| `seata-account` | 9104 | RM | `seata_account` |

依赖统一由 `services/pom.xml` 传入 nacos-discovery + openfeign；
Seata 用 `com.alibaba.cloud:spring-cloud-starter-alibaba-seata`，
由 SCA 2023.0.3.2 的 BOM 锁定到 `org.apache.seata:seata-spring-boot-starter:2.1.0`。
MyBatis 用 `mybatis-spring-boot-starter:3.0.5`。

## 二、数据库

**已经在本机 MySQL（`127.0.0.1:3306`，root/123456）上初始化完成。**
重新执行脚本也是安全的（脚本里是 `DROP TABLE IF EXISTS`，会重建表并重置演示数据）：

```bash
cd sql/seata
mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 01-seata-account.sql
mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 02-seata-storage.sql
mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 03-seata-order.sql
# 下面这个只有把 seata-server 配成 store.mode=db 才需要
mysql --host=127.0.0.1 --port=3306 --user=root --password=123456 < 04-seata-server-db.sql
```

| 库 | 表 | 初始数据 |
|---|---|---|
| `seata_account` | `t_account`、`undo_log` | 用户 1 余额 1000 |
| `seata_storage` | `t_storage`、`undo_log` | 商品 1 库存 100 |
| `seata_order` | `t_order`、`undo_log` | 空 |
| `seata`（Server 自用） | `global_table`、`branch_table`、`lock_table`、`distributed_lock` | — |

> `undo_log` 是 AT 模式的回滚日志表，**表名固定、每个参与事务的库都必须有**，
> 由 Seata 自动读写。它记录的是 SQL 执行前的「前镜像」，回滚时用前镜像反向补偿。

## 三、启动 Seata Server

```bash
cd sql/seata
docker compose up -d
docker logs -f seata-server
```

启动成功的标志：日志里出现 `Server started, service listen port: 8091`
和 `seata server started in xxxx millSeconds`。控制台在 http://127.0.0.1:7091
（默认账号密码 `seata` / `seata`）。

**关于 `store.mode`**：`server/application.yml` 里默认用的是 `file`，
也就是事务会话存在容器内的本地文件——不需要数据库、不需要 JDBC 驱动，开箱即用。
想改成 `db`（可以 `select` 出 `global_table` / `lock_table` 直接观察事务）看配置里的注释，
注意官方镜像出于授权原因没带 MySQL 驱动，而且镜像里是 JDK8，
要把 `mysql-connector-j-8.x.x.jar` 挂到容器的 `/seata-server/lib/jdbc/`。

**关于地址**：Seata Server 注册到 Nacos 的是它的**容器内网 IP**（172.x.x.x），
宿主机上的 Java 服务根本连不上——这是 Docker 部署 Seata 最常见的坑。
所以四个服务的 `application.yml` 里用的是 `registry.type: file` +
`service.grouplist.default: 127.0.0.1:8091`，直接连映射出来的 8091 端口。
如果哪天 Seata Server 的地址从你的机器上可达了，改回 `registry.type: nacos` 即可，
配置里留了注释模板。

## 四、启动四个服务

先构建（只需要一次）：

```bash
mvn -pl services/seata-order,services/seata-storage,services/seata-account,services/seata-business -am install -DskipTests
```

### 推荐：用脚本一键启停

```powershell
# 启动（日志写到 sql\seata\logs\<服务名>.log）
powershell -ExecutionPolicy Bypass -File sql\seata\start-all.ps1

# 停止（按端口找进程，不会误伤别的 java 进程）
powershell -ExecutionPolicy Bypass -File sql\seata\stop-all.ps1
```

### 手动启动

按依赖顺序（其实哪台先起都行，Feign 是懒加载的）：

```bash
java -jar services/seata-order/target/seata-order-0.0.1-SNAPSHOT.jar
java -jar services/seata-storage/target/seata-storage-0.0.1-SNAPSHOT.jar
java -jar services/seata-account/target/seata-account-0.0.1-SNAPSHOT.jar
java -jar services/seata-business/target/seata-business-0.0.1-SNAPSHOT.jar
```

四个都起来后，Nacos 服务列表里应该有 `seata-business` / `seata-order` / `seata-storage` / `seata-account`。

### ⚠️ 坑：配置写的是 9102，实际却绑到了别的端口

如果四个服务**全都**绑到同一个陌生端口（本项目实测是 `62802`），
并且先起来的那个抢到、后面的报 `Port 62802 was already in use`，那不是 yml 写错了。

**根因**：Spring Boot 的 relaxed binding 会把环境变量映射成配置项，
而 `ConfigurationPropertyName.adapt(name, '_')` 解析时**会丢掉空片段**，
于是 `SERVER__PORT=62802`（双下划线）被解析成 `server.port=62802`。
环境变量的优先级高于 `application.yml`，yml 里的端口就被顶掉了。

排查命令：

```powershell
# 看看有没有这类变量（注意是双下划线）
Get-ChildItem Env: | Where-Object { $_.Name -match 'SERVER' }
```

> 为什么 `--seata.enabled=false`、jar 内检查、grep `62802` 都查不出来？
> 因为它根本不在代码和配置文件里，而在**进程的环境变量**里。

**三种修法**（优先级从高到低）：

1. 启动脚本里清掉它：`Remove-Item Env:\SERVER__PORT`（`start-all.ps1` 已经这么做了）
2. 命令行显式覆盖：`java -jar xxx.jar --server.port=9102`（命令行参数优先级最高）
3. 换个干净的环境变量去启动（比如从「开始菜单」直接开一个新的终端）

同理，任何形如 `XXX__YYY` 的环境变量都会把 `xxx.yyy` 顶掉，
交付/部署时最好先 `Get-ChildItem Env:` 扫一眼有没有可疑的双下划线变量。

## 五、验证

### 1）正常下单 —— 全局提交

```bash
curl "http://127.0.0.1:9101/business/purchase?userId=1&productId=1&count=1&money=100"
```

预期：返回订单信息，`status=1`。数据库里看到余额变成 900、库存 99、订单 1 条。
各库的 `undo_log` **是空的** —— 因为全局提交时 Seata 会把 undo_log 删掉。

### 2）余额不足 —— 全局回滚（重点看这个）

```bash
curl "http://127.0.0.1:9101/business/purchase?userId=1&productId=1&count=1&money=10000"
```

余额只有 1000，第 3 步 `seata-account` 会抛「余额不足」。
此时第 1 步已经真的往 `seata_order.t_order` 插了一条订单，第 2 步已经真的把库存扣了，
但全局事务回滚会把它们撤销掉：

* `seata_order.t_order` 里**不会留下**这条订单
* `seata_storage.t_storage` 库存**还是 99**
* `seata_account.t_account` 余额**还是 900**

验证一下：

```bash
curl "http://127.0.0.1:9102/order/1"
curl "http://127.0.0.1:9103/storage/1"
curl "http://127.0.0.1:9104/account/1"
```

### 3）观察事务记录

```sql
-- 全局事务 / 分支事务
select xid, status, application_id, transaction_name from seata.global_table order by begin_time desc;
select branch_id, xid, resource_id, status from seata.branch_table;

-- 回滚过程中 undo_log 的存在与消失
select xid, branch_id, log_status, log_created from seata_order.undo_log;
```

回滚发生时，`undo_log` 里会短暂出现记录，补偿完成后被删除。

### 4）实测结果（本项目在本机跑通的记录）

从初始数据（余额 1000 / 库存 100 / 订单 0）出发：

| 用例 | 请求 | 响应 | 余额 | 库存 | 订单 |
|---|---|---|---|---|---|
| 1 正常下单 | `count=1&money=100` | `200`，`status=1` | **900** | **99** | **1** |
| 2 余额不足 | `count=1&money=10000` | `500`，余额不足 | 900 | 99 | 1 |
| 3 库存不足 | `count=9999&money=1` | `500`，库存不足 | 900 | 99 | 1 |

用例 2、3 的数据库状态与用例 1 之后**完全一致** —— 说明
「订单已插入、库存已扣减」这两个已经真实执行过的分支被成功撤销了。
三个库的 `undo_log` 和 `seata` 库的 `global_table` / `branch_table` 全部为 0。

对应的服务端日志：

```
# 用例 2：在 account 分支失败后
transaction 172.20.0.2:8091:1198780915382292486 will be rollback
[172.20.0.2:8091:1198780915382292486] rollback status: Rollbacked

# order 分支（已插入的订单被撤销）
branch register success, xid:...486, branchId:...487, lockKeys:t_order:2
Branch Rollbacking: ...486 ...487 jdbc:mysql://127.0.0.1:3306/seata_order
xid ...486 branch ...487, undo_log deleted with GlobalFinished
Branch Rollbacked result: PhaseTwo_Rollbacked
```

> 一个细节：`seata-order` 的一次「下单」会注册**两个**分支
> （`create` 和 `finish` 各是一次 `@Transactional`），所以能力上
> 「本地事务粒度 = 分支事务粒度」，不要以为一个服务只有一个分支。

## 六、几个必须知道的点

1. **分支事务失败必须抛异常**，不能吞。
   `seata-storage` / `seata-account` 的失败路径都是直接 `throw RuntimeException`，
   Spring 默认返回 HTTP 500，Feign 端因此抛异常，全局事务才会回滚。
   如果你写的 `@RestControllerAdvice` 把异常转成 HTTP 200，Seata 就会当成成功提交。

2. **`business` 侧的 `@RestControllerAdvice` 不影响回滚**。
   它在 `BusinessService.purchase()` 之外，切面此时已经完成回滚。但反过来，
   如果在 `BusinessService` 内部 try-catch 把异常吞掉，就真提交了。

3. **`enable-auto-data-source-proxy` 必须为 true**（默认就是）。
   它让 Seata 用 `DataSourceProxy` 包住真实数据源，SQL 才会经过 Seata。
   如果项目里自己定义了 `@Bean DataSource` 且没走代理，回滚会静默失效。

4. **`tx-service-group` 和 `vgroup-mapping` 要对得上**。
   报 `can not get cluster name in registry config 'default_tx_group'` 就是这里没配或 Seata Server 没注册上。

5. **本地 `@Transactional` 不能少**。Seata 的分支事务需要本地事务兜底，
   否则分支提交/回滚的语义会乱。

6. **`ProtostuffUndoLogParser ... class fail: io/protostuff/runtime/IdStrategy` 这个 WARN 可以忽略。**
   它只是说 Seata 想用 protostuff 序列化 `undo_log`，但项目里没引这个可选依赖，
   于是自动降级（下一行会打印 `Load compatible class io.seata.rm.datasource.undo.parser.spi.JacksonSerializer`）。
   功能完全正常。嫌日志吵的话，在 yml 里显式指定序列化器即可
   （配置项来自 `org.apache.seata.spring.boot.autoconfigure.properties.client.UndoProperties#logSerialization`）：

   ```yaml
   seata:
     client:
       undo:
         log-serialization: jackson
   ```

7. **`sql/seata/logs/` 是脚本产生的运行日志**（已在 `.gitignore` 里忽略），
   排查问题时可以进去看 `Tomcat initialized with port xxx` / `branch register success` /
   `Branch Rollbacked result: PhaseTwo_Rollbacked`。想重置只需删掉这个目录。

8. **Windows 上重新构建前必须先停服务**。正在运行的 JVM 会锁住 `target/*.jar`，
   `spring-boot-maven-plugin:repackage` 改名时失败：

   ```
   Unable to rename '...seata-business-0.0.1-SNAPSHOT.jar' to '...jar.original'
   ```

   更坑的是：此时 `jar` 插件已经把新的**瘦 jar** 写进去了，而 repackage 失败，
   于是 `target` 里留下一个**跑不起来**的 jar。所以顺序永远是
   `stop-all.ps1` → `mvn install` → `start-all.ps1`。

9. **别被 `mvn` 的退出码骗了**。用管道接 `Tee-Object` 时 `$LASTEXITCODE` 拿到的是
   PowerShell 的退出码，不是 maven 的。要判断构建是否真的成功，得看日志里有没有 `[ERROR]`。

## 七、关于 `io.seata` 和 `org.apache.seata` 双包名

IDE 会对 `io.seata.core.context.RootContext`、`io.seata.spring.annotation.GlobalTransactional`
打「已弃用」的提示。**这个提示是对的，应该改**，但原因值得说清楚。

### 它们是同一个东西吗？

是。Seata 捐给 Apache 后包名从 `io.seata` 改成了 `org.apache.seata`，
`io.seata` 被保留成**兼容壳**。用 `javap -c` 看它的字节码，每个方法都是直接转发：

```
public static java.lang.String io.seata.core.context.RootContext.getXID();
    0: invokestatic  #6   // Method org/apache/seata/core/context/RootContext.getXID:()Ljava/lang/String;
    3: areturn
```

所以行为完全等价——`getXID()` 读的是同一个 ThreadLocal，不会取不到 XID。
**不改也能正常跑**，只是会一直吃警告，并且未来大版本 Seata 会删掉这个兼容包。

### 改了之后切换的是"实现路径"

`@GlobalTransactional` 换成新包名后，日志里能直接看到拦截器换了：

```
# 改之前
Bean [com.flittly.service.BusinessService] ... would use interceptor
  [io.seata.integration.tx.api.interceptor.handler.GlobalTransactionalInterceptorHandler]
# 改之后
Bean [com.flittly.service.BusinessService] ... would use interceptor
  [org.apache.seata.integration.tx.api.interceptor.handler.GlobalTransactionalInterceptorHandler]
```

所以改完**必须重跑一遍验证**，不能只靠「编译过了」判断。

### 自动装配链其实早就是新包名了

| jar | 总条目 | `io/seata` | `org/apache/seata` |
|---|---|---|---|
| `seata-spring-boot-starter` | 27 | 0 | 9 |
| `seata-spring-autoconfigure-client` | 37 | 0 | 19 |
| `seata-spring-autoconfigure-core` | 55 | 0 | 35 |
| `seata-all` | 1624 | 194（兼容壳） | 1373 |

也就是说 Spring Boot 自动装配、`DataSourceProxy`、RM/TM 客户端全都是 `org.apache.seata`，
`io.seata` 只活在 `seata-all` 里等你迁移。**新项目直接写新包名就行**。

### 本项目已迁移的 5 处

| 文件 | 原 | 现 |
|---|---|---|
| `seata-business/BusinessService.java` | `io.seata.core.context.RootContext` | `org.apache.seata.core.context.RootContext` |
| 同上 | `io.seata.spring.annotation.GlobalTransactional` | `org.apache.seata.spring.annotation.GlobalTransactional` |
| `seata-order/OrderServiceImpl.java` | `io.seata.core.context.RootContext` | `org.apache.seata.core.context.RootContext` |
| `seata-storage/StorageServiceImpl.java` | 同上 | 同上 |
| `seata-account/AccountServiceImpl.java` | 同上 | 同上 |

⚠️ 但**不要无脑全局替换**：`io.seata` 只有 194 个类，
`org.apache.seata` 有 1373 个——如果某个 `io.seata` 在 `org.apache.seata` 下找不到对应类，就改不了。
改之前先确认目标类存在（`javap -cp <seata-all.jar> org.apache.seata.xxx.Yyy`）。
