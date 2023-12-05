package com.weikey.liuguangapigateway.filter;

import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.weikey.liuguangapicommon.model.dto.cache.InterfaceCacheDto;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoGetRequest;
import com.weikey.liuguangapicommon.model.dto.cache.UserCacheDto;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeCountRequest;
import com.weikey.liuguangapicommon.service.InterfaceFeignClient;
import com.weikey.liuguangapicommon.service.UserFeignClient;
import com.weikey.liuguangapigateway.config.RabbitmqConfig;
import com.weikey.liuguangapisdk.exception.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.weikey.liuguangapicommon.constant.RabbitMQConstant.INTERFACE_EXCHANGE_NAME;
import static com.weikey.liuguangapicommon.constant.RabbitMQConstant.INTERFACE_ROUTINGKEY;
import static com.weikey.liuguangapicommon.constant.RedisKeyConstant.*;
import static com.weikey.liuguangapigateway.utils.ResultUtils.handleError;
import static com.weikey.liuguangapisdk.utils.SignUtils.getSign;


@Component
@Slf4j
public class InterfaceInvokeFilter implements GatewayFilter, Ordered {

    private static final List<String> HOST_WHITE_LIST = Arrays.asList("127.0.0.1");

    /**
     * 5分钟，以秒为单位
     */
    private static final long FIVE_MINUTES = 5 * 60L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private InterfaceFeignClient interfaceFeignClient;

    private final Gson gson = new Gson();

    @Resource
    private RabbitTemplate rabbitTemplate;


    /**
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.请求日志 todo 优化；打印为1行
        // todo 这里可能有空指针异常？
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();
        String method = request.getMethodValue();
        String host = request.getLocalAddress().getHostString();
        int port = request.getLocalAddress().getPort();
        log.info("request id：" + request.getId());
        log.info("request path：" + path);
        log.info("request method：" + method);
        log.info("request host：" + host);
        log.info("request port：" + port);
        log.info("request param：" + request.getQueryParams());
        log.info("request headers：" + request.getHeaders());
        // 2.访问控制 - 黑白名单
        // 这里用白名单
        if (!HOST_WHITE_LIST.contains(host)) {
            return handleError(exchange, ApiError.IP_NOT_IN_WHITELIST);
        }
        // 3.用户鉴权（判断ak，sk是否合法）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String param = headers.getFirst("param");
        String sign = headers.getFirst("sign");
        String nonce = headers.getFirst("nonce");
        String timestamp = headers.getFirst("timestamp");

        if (StrUtil.hasBlank(accessKey, param, sign, nonce, timestamp)) {
            return handleError(exchange, ApiError.BLANK_HEADER);
        }

        // 3.1 根据accessKey查询得到secretKey和userId
        // 1)）查询缓存
        String userCacheStr = stringRedisTemplate.opsForValue().get(USER_KEY_PREFIX + accessKey);
        // 2）缓存数据不存在，查询数据库，然后将数据存入 redis。
        UserCacheDto userCacheDto = null;
        if (userCacheStr  == null) {
            try {
                userCacheDto = userFeignClient.getInvokeUser(accessKey);
            } catch (Exception e) {
                log.error("getInvokeUser error", e);
                return handleError(exchange, ApiError.INTERNAL_ERROR);
            }
            if (userCacheDto == null) {
                return handleError(exchange, ApiError.INVALID_ACCESSKEY);
            }
            stringRedisTemplate.opsForValue().set(USER_KEY_PREFIX + accessKey, gson.toJson(userCacheDto), 1L, TimeUnit.HOURS);
        } else { // 3）缓存数据存在，则直接从缓存中返回
            userCacheDto = gson.fromJson(userCacheStr, UserCacheDto.class);
        }


        String secretKey = userCacheDto.getSecretKey();
        // 3.2 签名是否一致
        if (!sign.equals(getSign(param, secretKey))) {
            return handleError(exchange, ApiError.SIGNATURE_FAILURE);
        }

        // 3.3 校验时间戳是否在当前时间的 5分钟 范围内
        Long queryTime = Long.valueOf(timestamp);
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - queryTime) / 1000 > FIVE_MINUTES) {
            return handleError(exchange, ApiError.SIGNATURE_EXPIRE);
        }

        // 3.4 查找 nonce 是否存在
        String value = stringRedisTemplate.opsForValue().get(NONCE_KEY_PREFIX + nonce);
        if (value == null) { // 不存在，保存 nonce，设置过期时间5分钟
            stringRedisTemplate.opsForValue().set(NONCE_KEY_PREFIX + nonce, "1", 5, TimeUnit.MINUTES);
        } else { // 存在，表示随机数使用过了，检验失败
            return handleError(exchange, ApiError.REQUEST_REPLAY);
        }

        // 4.接口校验：请求的模拟接口是否存在
        // todo 校验接口状态
        // 1)）查询缓存
        String interfaceStr = stringRedisTemplate.opsForValue().get(INTERFACE_KEY_PREFIX + path);
        InterfaceCacheDto interfaceCacheDto = null;
        // 2）缓存数据不存在，查询数据库，然后将数据存入 redis。
        if (interfaceStr  == null) {
            try {
                InterfaceInfoGetRequest interfaceInfoGetRequest = new InterfaceInfoGetRequest();
                interfaceInfoGetRequest.setUrl(path);
                interfaceCacheDto = interfaceFeignClient.getInterface(interfaceInfoGetRequest);
            } catch (Exception e) {
                log.error("getInterface error", e);
                return handleError(exchange, ApiError.INTERNAL_ERROR);
            }
            if (interfaceCacheDto == null) {
                return handleError(exchange, ApiError.INTERFACE_NOT_FOUNT);
            }
            stringRedisTemplate.opsForValue().set(INTERFACE_KEY_PREFIX + path, gson.toJson(interfaceCacheDto));
        } else {  // 3）缓存数据存在，则直接从缓存中返回
            interfaceCacheDto = gson.fromJson(interfaceStr, InterfaceCacheDto.class);
        }

        // 5.校验接口状态：开启/关闭
        Integer status = interfaceCacheDto.getStatus();
        if (status == 0) { // 接口关闭
            return handleError(exchange, ApiError.INTERFACE_CLOSE);
        }

        // 6.统计接口调用次数，同时校验调用次数是否还有剩余
        Boolean result = null;
        UserInterfaceInfoInvokeCountRequest userInterfaceInfoInvokeCountRequest = new UserInterfaceInfoInvokeCountRequest();
        userInterfaceInfoInvokeCountRequest.setInterfaceInfoId(interfaceCacheDto.getId());
        userInterfaceInfoInvokeCountRequest.setUserId(userCacheDto.getUserId());
        // todo try...catch改为全局异常处理
        try {
            // 统计接口调用次数
            // 使用【乐观锁】解决并发问题：update语句加条件 leftNum > 0
            // 在用户没有开通接口的情况下，用户调用接口的情况在表中没有记录，因此invokeCount执行的结果也是失败
            result = interfaceFeignClient.invokeCount(userInterfaceInfoInvokeCountRequest);
        } catch (Exception e) {
            log.error("invokeCount error", e);
            return handleError(exchange, ApiError.INTERNAL_ERROR);
        }
        // 接口剩余调用次数不足
        if (!result) {
            return handleError(exchange, ApiError.COUNT_NOT_ENOUGH);
        }

        return handleResponse(exchange, chain, userInterfaceInfoInvokeCountRequest);
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, UserInterfaceInfoInvokeCountRequest userInterfaceInfoInvokeCountRequest) {

        ServerHttpResponse originalResponse = exchange.getResponse();
        DataBufferFactory bufferFactory = originalResponse.bufferFactory();
        // 装饰者模式，对原先的Response对象添加新功能
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            // 调用完转发的接口后执行，对响应进行处理
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                HttpStatus statusCode = getStatusCode();
                // 对象是响应式的
                // 并且，响应正常 todo 这里判断响应是否正常的if...else分支写的可以吗？
                if (body instanceof Flux && statusCode.is2xxSuccessful()) {
                    Flux<? extends DataBuffer> fluxBody = (Flux<? extends DataBuffer>) body;
                    return super.writeWith(fluxBody.buffer().map(dataBuffer -> {

                        DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
                        DataBuffer join = dataBufferFactory.join(dataBuffer);

                        byte[] content = new byte[join.readableByteCount()];
                        join.read(content);
                        // 释放掉内存
                        DataBufferUtils.release(join);
                        // 返回的数据
                        String respDataStr = new String(content, StandardCharsets.UTF_8);

                        // 8.响应日志
                        log.info("response statusCode: " + statusCode);
                        log.info("response data: " + respDataStr);

                        byte[] respData = respDataStr.getBytes();
                        return bufferFactory.wrap(respData);
                    }));
                } else { // 响应异常
                    // todo 这里响应返回很慢，backend一直拿不到响应
                    // 9.todo 调用失败，返回规范错误码
                    log.error("revoke fail, response statusCode: " + statusCode);

                    // 发送消息，回滚被扣减的调用次数
                    rabbitTemplate.convertAndSend(INTERFACE_EXCHANGE_NAME, INTERFACE_ROUTINGKEY, userInterfaceInfoInvokeCountRequest);

                    return handleError(exchange, ApiError.INVOKE_FAILURE);
                }
            }
        };
        // 7.请求转发，调用接口
        // 设置response对象为修饰过的
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }


    /**
     * 响应的处理逻辑要想起作用，自定义过滤器的 Order 值（优先级）必须设置为 -2 或者 更小
     * @return
     */
    @Override
    public int getOrder() {
        return -2;
    }
}