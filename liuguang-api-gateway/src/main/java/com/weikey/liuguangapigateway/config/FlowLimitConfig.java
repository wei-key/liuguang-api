package com.weikey.liuguangapigateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayParamFlowItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import com.alibaba.csp.sentinel.adapter.gateway.sc.exception.SentinelGatewayBlockExceptionHandler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
public class FlowLimitConfig {
    private final List<ViewResolver> viewResolvers;
    private final ServerCodecConfigurer serverCodecConfigurer;

    public FlowLimitConfig(ObjectProvider<List<ViewResolver>> viewResolversProvider, ServerCodecConfigurer serverCodecConfigurer) {
        this.viewResolvers = viewResolversProvider.getIfAvailable(Collections::emptyList);
        this.serverCodecConfigurer = serverCodecConfigurer;
    }

    /**
     * 初始化一个限流的过滤器
     *
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public GlobalFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    /**
     * 配置限流的异常处理器
     *
     * @return
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SentinelGatewayBlockExceptionHandler
    sentinelGatewayBlockExceptionHandler() {
        return new SentinelGatewayBlockExceptionHandler(viewResolvers,
                serverCodecConfigurer);
    }


    /**
     * 加载规则
     */
    @PostConstruct
    public void doInit() {
        initCustomizedApis();
        initGatewayRules();
    }

    /**
     * 自定义API分组
     */
    private void initCustomizedApis() {
        Set<ApiDefinition> definitions = new HashSet<>();
        // 设置分组名称
        ApiDefinition api = new ApiDefinition("interface-service-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    // 设置规则
                    add(new ApiPathPredicateItem().setPattern("/api/interface-service/**")
                                    .setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX)); // url前缀匹配
                }});
        definitions.add(api);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
    }

    /**
     * 配置流控规则
     */
    public void initGatewayRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        // 设置api分组名称，需要与 initCustomizedApis 方法中定义的分组名称一致
        rules.add(new GatewayFlowRule("interface-service-api")
                .setResourceMode(SentinelGatewayConstants.RESOURCE_MODE_CUSTOM_API_NAME)
                // 限流阈值 todo 上线后阈值考虑多少合适
                .setCount(5)
                // 时间窗口
                .setIntervalSec(1)
                // 针对请求头accessKey进行限流
                .setParamItem(new GatewayParamFlowItem()
                        .setParseStrategy(SentinelGatewayConstants.PARAM_PARSE_STRATEGY_HEADER)
                        .setFieldName("accessKey")
                )
        );
        GatewayRuleManager.loadRules(rules);
    }
}
