package com.weikey.liuguangapiorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.weikey.liuguangapiorder.mapper")
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.weikey")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.weikey.liuguangapicommon.service"})
public class LiuguangApiOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiuguangApiOrderApplication.class, args);
    }

}
