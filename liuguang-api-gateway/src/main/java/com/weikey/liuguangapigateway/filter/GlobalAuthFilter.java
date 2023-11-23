package com.weikey.liuguangapigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weikey.liuguangapicommon.model.enums.ErrorCode;
import com.weikey.liuguangapicommon.model.response.BaseResponse;
import com.weikey.liuguangapicommon.utils.JWTUtils;
import com.weikey.liuguangapicommon.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


@Component
@Slf4j
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 不需要进行登录校验的接口路径
     * todo 是否还有其他？
     */
    private static final List<String> NEEDLESS_LOGIN_PATHS = Arrays.asList("/api/user/login", "/api/user/register");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        log.info("path: {}", path);
        // 如果是sdk接口调用，直接放行
        if (antPathMatcher.match("/api/interface-service/**", path)) {
            return chain.filter(exchange);
        }
        // 1.接口安全性校验
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON_UTF8);
        // 判断路径中是否包含 inner，只允许内部调用
        if (antPathMatcher.match("/**/inner/**", path)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap("无权限".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer)); // 作为响应体写回
        }
        // 2.用户登录校验
        // 2.1 不需要进行登录校验的接口直接放行
        if (NEEDLESS_LOGIN_PATHS.contains(path)) {
            return chain.filter(exchange);
        }
        // 2.2 从请求头中取出 token，进行 token 校验
        String header = request.getHeaders().getFirst(JWTUtils.AUTHORIZATION);
        boolean resutl = JWTUtils.verify(header);
        if (!resutl) { // 校验失败
            BaseResponse baseResponse = ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, "登录校验失败");
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = null;
            try {
                dataBuffer = dataBufferFactory.wrap(objectMapper.writeValueAsBytes(baseResponse));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            return response.writeWith(Mono.just(dataBuffer)); // 作为响应体写回
        }

        return chain.filter(exchange);
    }

    /**
     * 优先级提到最高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
