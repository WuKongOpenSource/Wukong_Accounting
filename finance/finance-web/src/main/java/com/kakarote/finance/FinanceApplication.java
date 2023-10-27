package com.kakarote.finance;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author zhangzhiwei
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.kakarote.finance.mapper"})
@EnableFeignClients(basePackages = {"com.kakarote"})
public class FinanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FinanceApplication.class, args);
    }
}
