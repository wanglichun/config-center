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
 * MySQL操作拦截器
 * 自动记录所有数据库操作的日志
 */
@Slf4j
@Aspect
@Component
public class MysqlInterceptor {
    
    @Autowired
    private LogSenderService logSenderService;
    
    /**
     * 拦截所有Mapper接口的方法调用
     */
    @Around("execution(* com.example.configcenter.mapper.*.*(..))")
    public Object traceMysqlOperation(ProceedingJoinPoint point) throws Throwable {
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
            
            // 构建SQL信息
            String sql = buildSqlInfo(methodName, args);
            int rowsAffected = getRowsAffected(result);
            
            // 发送MySQL操作日志
//            logSenderService.sendMysqlLog(
//                context.getTraceId(),
//                spanId,
//                parentSpanId,
//                methodName,
//                sql,
//                result,
//                duration,
//                rowsAffected,
//                null
//            );
            
            log.debug("MySQL操作完成: method={}, duration={}ms, rowsAffected={}", 
                     methodName, duration, rowsAffected);
            
            return result;
            
        } catch (Exception e) {
            // 计算执行时间
            long duration = System.currentTimeMillis() - startTime;
            errorMessage = e.getMessage();
            
            // 发送错误日志
//            logSenderService.sendMysqlLog(
//                context.getTraceId(),
//                spanId,
//                parentSpanId,
//                methodName,
//                buildSqlInfo(methodName, args),
//                null,
//                duration,
//                0,
//                errorMessage
//            );
            
            log.error("MySQL操作失败: method={}, duration={}ms, error={}", 
                     methodName, duration, errorMessage, e);
            
            throw e;
        }
    }
    
    /**
     * 构建SQL信息
     */
    private String buildSqlInfo(String methodName, Object[] args) {
        StringBuilder sql = new StringBuilder();
        sql.append(methodName);
        
        if (args != null && args.length > 0) {
            sql.append("(");
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sql.append(", ");
                if (args[i] != null) {
                    String argStr = args[i].toString();
                    // 限制参数长度，避免日志过长
                    if (argStr.length() > 100) {
                        argStr = argStr.substring(0, 100) + "...";
                    }
                    sql.append(argStr);
                } else {
                    sql.append("null");
                }
            }
            sql.append(")");
        }
        
        return sql.toString();
    }
    
    /**
     * 获取影响的行数
     */
    private int getRowsAffected(Object result) {
        if (result == null) {
            return 0;
        }
        
        // 根据返回类型判断影响的行数
        if (result instanceof Integer) {
            return (Integer) result;
        } else if (result instanceof Long) {
            return ((Long) result).intValue();
        } else if (result instanceof Boolean) {
            return (Boolean) result ? 1 : 0;
        } else if (result instanceof java.util.List) {
            return ((java.util.List<?>) result).size();
        }
        
        return 1; // 默认返回1
    }
} 