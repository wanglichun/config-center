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
        Cookie[] cookies = request.getCookies();
        Context context = new Context();
        for (Cookie cookie : cookies) {
            if (Constant.USER_EMAIL.equals(cookie.getName())) {
                context.setUserEmail(cookie.getValue());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        ContextManager.deleteContext();
    }
}
