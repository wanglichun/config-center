package com.example.configcenter.interceptor;

import com.example.configcenter.log.LogSenderService;
import com.example.configcenter.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;


/**
 * 链路追踪拦截器
 */
@Slf4j
@Component
public class TraceInterceptor implements HandlerInterceptor {
    
    @Autowired
    private LogSenderService logSenderService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取或生成TraceId
        String traceId = request.getHeader("X-Trace-Id");
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceContext.generateTraceId();
        }
        
        // 生成请求ID
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isEmpty()) {
            requestId = "req_" + UUID.randomUUID().toString().substring(0, 8);
        }
        
        // 创建根Span
        String spanId = TraceContext.generateSpanId();
        TraceContext context = new TraceContext();
        context.setTraceId(traceId);
        context.setSpanId(spanId);
        context.setRequestId(requestId);
        context.setUserEmail(getCurrentUserId(request));
        context.setStartTime(System.currentTimeMillis());
        
        // 添加请求信息到标签
        context.addTag("method", request.getMethod());
        context.addTag("url", request.getRequestURI());
        context.addTag("clientIp", getClientIp(request));
        context.addTag("userAgent", request.getHeader("User-Agent"));
        
        TraceContext.setContext(context);
        
        // 将TraceId添加到响应头
        response.addHeader("X-Trace-Id", traceId);
        response.addHeader("X-Request-Id", requestId);
        
        log.debug("开始处理请求: traceId={}, requestId={}, url={}", traceId, requestId, request.getRequestURI());
        
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TraceContext context = TraceContext.getContext();
        if (context != null) {
            long duration = System.currentTimeMillis() - context.getStartTime();
            String method = request.getMethod();
            String url = request.getRequestURI();
            int statusCode = response.getStatus();
            String clientIp = getClientIp(request);
            String userId = context.getUserEmail();
            
            String errorMessage = null;
            if (ex != null) {
                errorMessage = ex.getMessage();
            }
            
            // 发送API请求日志
            logSenderService.sendApiLog(
                context.getTraceId(),
                context.getSpanId(),
                null, // 根Span没有父Span
                method,
                url,
                statusCode,
                duration,
                clientIp,
                userId,
                errorMessage
            );
            
            log.debug("请求处理完成: traceId={}, duration={}ms, status={}", 
                     context.getTraceId(), duration, statusCode);
            
            TraceContext.clearContext();
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId(HttpServletRequest request) {
        // 这里可以从JWT token或session中获取用户ID
        // 暂时返回一个默认值
        return "user_" + System.currentTimeMillis() % 1000;
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