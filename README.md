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
