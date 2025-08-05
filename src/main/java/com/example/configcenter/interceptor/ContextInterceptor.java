package com.example.configcenter.interceptor;

import com.example.configcenter.constant.Constant;
import com.example.configcenter.context.Context;
import com.example.configcenter.context.ContextManager;
import com.example.configcenter.log.LogSenderService;
import com.example.configcenter.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
@Slf4j
public class ContextInterceptor implements HandlerInterceptor {

    @Autowired
    LogSenderService logSenderService;

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


        ContextManager.setContext(context);
        return true;
    }

    private void buildContext(Context context, HttpServletRequest request) {
        // 从请求头获取或生成TraceId
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = generateTraceId();
        }

        // 生成请求ID
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isEmpty()) {
            requestId = "req_" + UUID.randomUUID().toString().substring(0, 8);
        }

        // 创建根Span
        String spanId = TraceContext.generateSpanId();
        context.setTraceId(traceId);
        context.setSpanId(spanId);
        context.setRequestId(requestId);
        context.setStartTime(System.currentTimeMillis());

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {

            ContextManager.deleteContext();

    }

    public static String generateTraceId() {
        return "trace_" + UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
