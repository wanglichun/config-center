package com.example.configcenter.interceptor;

import com.example.configcenter.constant.Constant;
import com.example.configcenter.context.Context;
import com.example.configcenter.context.ContextManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class ContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 创建Context对象
        Context context = new Context();
        
        // 安全地获取cookies，避免空指针异常
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (Constant.USER_EMAIL.equals(cookie.getName())) {
                    context.setUserEmail(cookie.getValue());
                    log.debug("Set user email from cookie: {}", cookie.getValue());
                    break; // 找到后就可以退出循环
                }
            }
        }
        
        // 将Context设置到ContextManager中
        ContextManager.setContext(context);
        log.debug("Context set for request: {}", request.getRequestURI());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        // 清理Context
        ContextManager.deleteContext();
        log.debug("Context cleaned for request: {}", request.getRequestURI());
    }
}
