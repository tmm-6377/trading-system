# Trading System Demo

基于 Spring Boot 3、Spring Cloud Alibaba、MyBatis-Plus、MySQL、Redis、Nacos 的商品交易系统示例，包含 `user-service` 和 `merchant-service` 两个微服务，并采用 DDD 分层组织代码。

## 模块说明

- `trading-common`：公共响应模型与异常
- `trading-api`：商家服务 Feign 接口与 DTO
- `user-service`：用户账户、下单、订单/流水查询
- `merchant-service`：库存管理、订单处理、结算任务

## 快速启动

```bash
docker-compose up -d
mvn test
cd user-service && mvn spring-boot:run
cd ../merchant-service && mvn spring-boot:run
```

## 主要 API

### 用户服务（8081）

- `POST /api/v1/users/{userId}/account/recharge`
- `GET /api/v1/users/{userId}/account`
- `GET /api/v1/users/{userId}/transactions`
- `POST /api/v1/orders`
- `GET /api/v1/orders/{orderNo}`
- `GET /api/v1/users/{userId}/orders`

### 商家服务（8082）

- `POST /api/v1/merchants/{merchantId}/inventory`
- `PUT /api/v1/merchants/{merchantId}/inventory/{sku}`
- `GET /api/v1/merchants/{merchantId}/inventory`
- `GET /api/v1/merchants/{merchantId}/inventory/{sku}`
- `GET /api/v1/merchants/{merchantId}/account`
- `GET /api/v1/merchants/{merchantId}/settlements`

### 内部 API

- `POST /internal/inventory/check-stock`
- `POST /internal/orders/confirm`
- `POST /internal/inventory/rollback`

## 下单流程

1. 用户服务通过 Feign 调用商家服务检查库存
2. 使用 Redis 分布式锁保护同一用户同一商品的重复下单
3. 用户账户扣款（MyBatis-Plus 乐观锁）
4. 商家服务扣减库存、增加商家账户余额
5. 用户服务保存订单与账户流水

## 结算任务

商家服务默认每天凌晨 2 点执行结算，可通过 `business.settlement.cron` 调整。结算逻辑会按商家聚合已售商品金额，与商家账户余额进行对账并记录 `MATCH/MISMATCH` 结果。

## Seata 分布式事务

本项目使用 Seata AT 模式实现分布式事务，确保用户下单流程的数据一致性。

### 事务流程

1. **用户服务**（TM - 事务管理器）：
   - 开启全局事务 `@GlobalTransactional`
   - 扣减用户余额（分支事务1）
   - 调用商家服务确认订单
   - 保存订单记录（分支事务2）

2. **商家服务**（RM - 资源管理器）：
   - 扣减库存（分支事务3）
   - 商家账户收款（分支事务4）

3. **Seata Server**（TC - 事务协调器）：
   - 协调全局事务
   - 管理分支事务
   - 出现异常时自动回滚所有分支

### 启动顺序

```bash
# 1. 启动基础设施（包含 Seata Server）
docker-compose up -d

# 2. 等待 Seata Server 启动完成
docker logs -f trading-seata

# 3. 访问 Seata 控制台
open http://localhost:7091
# 用户名：seata，密码：seata

# 4. 启动微服务
cd user-service && mvn spring-boot:run
cd merchant-service && mvn spring-boot:run
```

### Seata 控制台

访问 http://localhost:7091 查看：
- 全局事务列表
- 分支事务详情
- 全局锁信息
- 事务统计

### 测试分布式事务

模拟异常回滚：
```bash
# 在商家服务的 processOrder 方法中抛出异常
# 观察 Seata 是否自动回滚用户余额
```

正常提交：
```bash
# 正常下单流程
# 观察 Seata 全局事务两阶段提交
```
