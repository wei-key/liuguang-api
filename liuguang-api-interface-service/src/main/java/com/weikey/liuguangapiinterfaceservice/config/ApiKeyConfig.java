package com.weikey.liuguangapiinterfaceservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration // 一定要加
@PropertySource(value = {"classpath:apiKey-dev.yml"}, factory = YAMLPropertySourceFactory.class)
@Data
public class ApiKeyConfig {

    @Value("${api.resou.access-key}")
    private String accessKey;

    @Value("${api.resou.secret-key}")
    private String secretKey;

    @Value("${api.weather.appid}")
    private String appid;

    @Value("${api.weather.appsecret}")
    private String appsecret;
}

