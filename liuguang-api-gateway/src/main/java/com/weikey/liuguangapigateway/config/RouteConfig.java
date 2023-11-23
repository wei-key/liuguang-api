package com.weikey.liuguangapigateway.config;

import com.weikey.liuguangapigateway.filter.InterfaceInvokeFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, InterfaceInvokeFilter interfaceInvokeFilter) {
        return builder.routes()
                .route("liuguang-api-interface-service", r -> r.path("/api/interface-service/**")
                        .filters(f -> f.filter(interfaceInvokeFilter)
                                .addRequestHeader("source","gateway")
                        ).uri("lb://liuguang-api-interface-service")
                ).build();
    }

}