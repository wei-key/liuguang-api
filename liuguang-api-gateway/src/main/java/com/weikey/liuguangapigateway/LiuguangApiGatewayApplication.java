package com.weikey.liuguangapigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.weikey.liuguangapicommon.service"})
@ComponentScan("com.weikey")
public class LiuguangApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiuguangApiGatewayApplication.class, args);
    }

}
