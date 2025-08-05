package com.example.configcenter.interceptor;

import com.example.configcenter.log.LogSenderService;
import com.example.configcenter.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redis操作拦截器
 * 自动记录所有Redis操作的日志
 */
@Slf4j
@Aspect
@Component
public class RedisInterceptor {
    
    @Autowired
    private LogSenderService logSenderService;
    
    /**
     * 拦截所有RedisTemplate的方法调用
     */
    @Around("execution(* org.springframework.data.redis.core.RedisTemplate.*(..))")
    public Object traceRedisTemplateOperation(ProceedingJoinPoint point) throws Throwable {
        return traceRedisOperation(point, "RedisTemplate");
    }
    
    /**
     * 拦截所有CacheService的方法调用
     */
    @Around("execution(* com.example.configcenter.service.CacheService.*(..))")
    public Object traceCacheServiceOperation(ProceedingJoinPoint point) throws Throwable {
        return traceRedisOperation(point, "CacheService");
    }
    
    /**
     * 通用的Redis操作追踪方法
     */
    private Object traceRedisOperation(ProceedingJoinPoint point, String component) throws Throwable {
        TraceContext context = TraceContext.getContext();
        if (context == null) {
            // 如果没有TraceContext，直接执行原方法
            return point.proceed();
        }
        
        String methodName = point.getSignature().getName();
        String className = point.getTarget().getClass().getSimpleName();
        Object[] args = point.getArgs();
        
        // 生成子Span
        String spanId = TraceContext.generateSpanId();
        String parentSpanId = context.getSpanId();
        
        long startTime = System.currentTimeMillis();
        Object result = null;
        String errorMessage = null;
        
        try {
            // 执行原方法
            result = point.proceed();
            
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            
            // 构建Redis操作信息
            String key = extractKey(args);
            Object value = extractValue(args);
            boolean hit = isCacheHit(result, methodName);
            
            // 发送Redis操作日志
            logSenderService.sendRedisLog(
                context.getTraceId(),
                spanId,
                parentSpanId,
                methodName,
                key,
                value,
                duration,
                hit,
                null
            );
            
            log.debug("Redis操作完成: component={}, method={}, key={}, hit={}, duration={}ms", 
                     component, methodName, key, hit, duration);
            
            return result;
            
        } catch (Exception e) {
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            errorMessage = e.getMessage();
            
            // 构建Redis操作信息
            String key = extractKey(args);
            Object value = extractValue(args);
            
            // 发送错误日志
            logSenderService.sendRedisLog(
                context.getTraceId(),
                spanId,
                parentSpanId,
                methodName,
                key,
                value,
                duration,
                false,
                errorMessage
            );
            
            log.error("Redis操作失败: component={}, method={}, key={}, duration={}ms, error={}", 
                     component, methodName, key, duration, errorMessage, e);
            
            throw e;
        }
    }
    
    /**
     * 从参数中提取Redis键
     */
    private String extractKey(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 第一个参数通常是键
        if (args[0] instanceof String) {
            return (String) args[0];
        }
        
        return null;
    }
    
    /**
     * 从参数中提取Redis值
     */
    private Object extractValue(Object[] args) {
        if (args == null || args.length < 2) {
            return null;
        }
        
        // 第二个参数通常是值
        if (args.length >= 2) {
            return args[1];
        }
        
        return null;
    }
    
    /**
     * 判断是否为缓存命中
     */
    private boolean isCacheHit(Object result, String methodName) {
        if (result == null) {
            return false;
        }
        
        // 根据方法名和结果判断是否为缓存命中
        switch (methodName.toLowerCase()) {
            case "get":
            case "getcache":
                return result != null;
            case "haskey":
                return Boolean.TRUE.equals(result);
            default:
                return true; // 其他操作默认认为是成功的
        }
    }
} 