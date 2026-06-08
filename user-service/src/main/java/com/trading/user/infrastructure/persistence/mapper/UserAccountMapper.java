package com.trading.user.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.trading.user.infrastructure.persistence.entity.UserAccountPO;

/**
 * 用户账户 MyBatis-Plus Mapper 接口
 *
 * <p>继承 {@link BaseMapper} 获得基础的 CRUD 操作（insert、selectById、updateById 等）。</p>
 *
 * <p>主要使用的继承方法：</p>
 * <ul>
 *   <li>{@code insert(T)} - 插入新账户记录</li>
 *   <li>{@code selectOne(Wrapper)} - 根据条件查询单条账户</li>
 *   <li>{@code updateById(T)} - 根据主键更新账户（含乐观锁版本校验）</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
public interface UserAccountMapper extends BaseMapper<UserAccountPO> {
}
