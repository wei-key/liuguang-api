package com.weikey.liuguangapiinterfaceservice.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        SourceInterceptor interceptor = new SourceInterceptor();
        registry.addInterceptor(interceptor).addPathPatterns("/**"); // 拦截所有请求
    }
}
