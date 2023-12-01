package com.weikey.liuguangapigateway.filter;

import cn.hutool.core.util.StrUtil;
import com.weikey.liuguangapicommon.model.dto.interfaceInfo.InterfaceInfoGetRequest;
import com.weikey.liuguangapicommon.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeCountRequest;
import com.weikey.liuguangapicommon.model.entity.InterfaceInfo;
import com.weikey.liuguangapicommopackage com.weikey.liuguangapiinterfaceservice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SourceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //获取请求头中的source值
        String value = request.getHeader("source");
        //获取请求的本地地址
        String localAddr = request.getLocalAddr();
        //判断source值是否为gateway
        if (!"gateway".equals(value)) {
            //若不是，则记录日志，并返回403状态码
            log.error("Request directly access to the interface without passing through the gateway. Request origin: " + localAddr);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        //若是，则返回true
        return true;
    }

}r;
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

    private static final String REDIS_KEY = "api:sign:nonce:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private InterfaceFeignClient interfaceFeignClient;


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

        // 远程调用，去数据库根据accessKey查询secretKey
        User user = null;
        try {
            user = userFeignClient.getInvokeUser(accessKey); // todo 优化
        } catch (Exception e) {
            log.error("getInvokeUser error", e);
            return handleError(exchange, ApiError.INTERNAL_ERROR);
        }
        if (user == null) {
            return handleError(exchange, ApiError.INVALID_ACCESSKEY);
        }

        String secretKey = user.getSecretKey();
        // 签名是否一致
        if (!sign.equals(getSign(param, secretKey))) {
            return handleError(exchange, ApiError.SIGNATURE_FAILURE);
        }

        // 校验时间戳是否在当前时间的 5分钟 范围内
        Long queryTime = Long.valueOf(timestamp);
        long currentTimeMillis = System.currentTimeMillis();
        if ((currentTimeMillis - queryTime) / 1000 > FIVE_MINUTES) {
            return handleError(exchange, ApiError.SIGNATURE_EXPIRE);
        }

        // 查找 nonce 是否存在
        String value = stringRedisTemplate.opsForValue().get(REDIS_KEY + nonce);
        if (value == null) { // 不存在，保存 nonce，设置过期时间5分钟
            stringRedisTemplate.opsForValue().set(REDIS_KEY + nonce, "1", 5, TimeUnit.MINUTES);
        } else { // 存在，表示随机数使用过了，检验失败
            return handleError(exchange, ApiError.REQUEST_REPLAY);
        }

        // 4.接口校验：请求的模拟接口是否存在，参数：请求地址、请求方式 todo 再加一个参数：请求参数
        // todo 校验接口状态
        InterfaceInfo interfaceInfo = null;
        InterfaceInfoGetRequest interfaceInfoGetRequest = new InterfaceInfoGetRequest();
        interfaceInfoGetRequest.setUrl(path); // 请求路径
        interfaceInfoGetRequest.setMethod(method);
        try {
            interfaceInfo = interfaceFeignClient.getInterface(interfaceInfoGetRequest);
        } catch (Exception e) {
            log.error("getInterface error", e);
            return handleError(exchange, ApiError.INTERNAL_ERROR);
        }
        if (interfaceInfo == null) {
            return handleError(exchange, ApiError.INTERFACE_NOT_FOUNT);
        }

        // 5.统计接口调用次数，同时校验调用次数是否还有剩余
        Boolean result = null;
        UserInterfaceInfoInvokeCountRequest userInterfaceInfoInvokeCountRequest = new UserInterfaceInfoInvokeCountRequest();
        userInterfaceInfoInvokeCountRequest.setInterfaceInfoId(interfaceInfo.getId());
        userInterfaceInfoInvokeCountRequest.setUserId(user.getId());
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

        return handleResponse(exchange, chain);
    }

    private Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain) {

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

                        // 7.响应日志
                        log.info("response statusCode: " + statusCode);
                        log.info("response data: " + respDataStr);

                        byte[] respData = respDataStr.getBytes();
                        return bufferFactory.wrap(respData);
                    }));
                } else { // 响应异常
                    // todo 这里响应返回很慢，backend一直拿不到响应
                    // 8.todo 调用失败，返回规范错误码
                    log.error("revoke fail, response statusCode: " + statusCode);
                    return handleError(exchange, ApiError.INVOKE_FAILURE);
                }
            }
        };
        // 6.请求转发，调用接口
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