package com.weikey.liuguangapigateway.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weikey.liuguangapisdk.exception.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static com.weikey.liuguangapisdk.constant.HeaderConstant.RESP_ERROR_NAME;
import static com.weikey.liuguangapisdk.constant.HeaderConstant.RESP_ERROR_VALUE;

@Slf4j
public class ResultUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理错误：不转发请求，直接返回错误信息
     * @param exchange
     * @param apiError
     * @return 返回错误码
     */
    public static Mono<Void> handleError(ServerWebExchange exchange, ApiError apiError) {
        ServerHttpResponse response = exchange.getResponse();
        // todo 设置响应头，当响应头中有此字段时，表示响应数据为错误信息
        response.getHeaders().set(RESP_ERROR_NAME, RESP_ERROR_VALUE);
        response.setStatusCode(HttpStatus.OK); // todo 可以吗？
        return response.writeWith(Mono.fromSupplier(() -> {
            DataBufferFactory bufferFactory = response.bufferFactory();
            try {
                log.error("{}: {}", apiError.getCode(), apiError.getMessage());
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(apiError));
            } catch (JsonProcessingException e) {
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}
