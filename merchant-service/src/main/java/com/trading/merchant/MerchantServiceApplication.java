package com.trading.merchant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = "com.trading")
@EnableDiscoveryClient
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.trading.merchant.infrastructure.persistence.mapper")
public class MerchantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MerchantServiceApplication.class, args);
    }
}
