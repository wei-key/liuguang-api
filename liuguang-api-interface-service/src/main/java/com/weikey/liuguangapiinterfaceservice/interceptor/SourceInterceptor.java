package com.weikey.liuguangapiinterfaceservice.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class SourceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String value = request.getHeader("source");
        String localAddr = request.getLocalAddr();
        if (!"gateway".equals(value)) {
            log.error("Request directly access to the interface without passing through the gateway. Request origin: " + localAddr);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }

}
