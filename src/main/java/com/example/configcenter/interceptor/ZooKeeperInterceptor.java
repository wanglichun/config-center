package com.example.configcenter.interceptor;

import com.example.configcenter.log.LogSenderService;
import com.example.configcenter.trace.TraceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ZooKeeper操作拦截器
 * 自动记录所有ZooKeeper操作的日志
 */
@Slf4j
@Aspect
@Component
public class ZooKeeperInterceptor {
    
    @Autowired
    private LogSenderService logSenderService;
    
    /**
     * 拦截所有ZooKeeperService接口的方法调用
     */
    @Around("execution(* com.example.configcenter.service.ZooKeeperService.*(..))")
    public Object traceZooKeeperOperation(ProceedingJoinPoint point) throws Throwable {
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
            
            // 构建ZooKeeper操作信息
            String path = extractPath(args);
            Object data = extractData(args);
            
            // 发送ZooKeeper操作日志
            logSenderService.sendZkLog(
                context.getTraceId(),
                spanId,
                parentSpanId,
                methodName,
                path,
                data,
                duration,
                null
            );
            
            log.debug("ZooKeeper操作完成: method={}, path={}, duration={}ms", 
                     methodName, path, duration);
            
            return result;
            
        } catch (Exception e) {
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            errorMessage = e.getMessage();
            
            // 构建ZooKeeper操作信息
            String path = extractPath(args);
            Object data = extractData(args);
            
            // 发送错误日志
            logSenderService.sendZkLog(
                context.getTraceId(),
                spanId,
                parentSpanId,
                methodName,
                path,
                data,
                duration,
                errorMessage
            );
            
            log.error("ZooKeeper操作失败: method={}, path={}, duration={}ms, error={}", 
                     methodName, path, duration, errorMessage, e);
            
            throw e;
        }
    }
    
    /**
     * 从参数中提取路径
     */
    private String extractPath(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        // 第一个参数通常是路径
        if (args[0] instanceof String) {
            return (String) args[0];
        }
        
        return null;
    }
    
    /**
     * 从参数中提取数据
     */
    private Object extractData(Object[] args) {
        if (args == null || args.length < 2) {
            return null;
        }
        
        // 第二个参数通常是数据
        if (args.length >= 2 && args[1] instanceof String) {
            return args[1];
        }
        
        return null;
    }
} 