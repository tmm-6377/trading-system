package com.trading.merchant.infrastructure.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 *
 * <p>配置 MyBatis-Plus 插件：</p>
 * <ul>
 *   <li>乐观锁拦截器：自动处理 {@code @Version} 注解的版本号递增，
 *       防止并发更新时的数据覆盖问题</li>
 * </ul>
 *
 * @author Trading System
 * @since 1.0.0
 */
@Configuration
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     *
     * <p>注册乐观锁拦截器，在执行 UPDATE 时自动附加版本号条件，
     * 若版本号不匹配则更新失败，由上层捕获并重试。</p>
     *
     * @return 已注册乐观锁插件的拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 注册乐观锁拦截器，配合实体类中的 @Version 字段使用
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
