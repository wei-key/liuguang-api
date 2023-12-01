package com.weikey.liuguangapiinterfaceservice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SourceInterceptor implements HandlerInterceptor {

    /**
     * 判断请求是否经过网关转发
     *
     * @param request
     * @param response
     * @param handler
     * @return
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 获取请求头中的source值
        String value = request.getHeader("source");
        // 获取请求的本地地址
        String localAddr = request.getLocalAddr();
        // 如果source值不是gateway，则拒绝访问
        if (!"gateway".equals(value)) {
            log.error("Request directly access to the interface without passing through the gateway. Request origin: " + localAddr);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }

}
