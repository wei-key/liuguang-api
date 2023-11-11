package com.weikey.liuguangapisdk.config;

import com.weikey.liuguangapisdk.client.ApiClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("api.client")
@Data
// @ComponentScan
public class ApiClientConfig {

    private String accessKey;

    private String secretKey;

    @Bean("apiClient")
    public ApiClient getApiClient() {
        return new ApiClient(accessKey, secretKey);
    }

}
