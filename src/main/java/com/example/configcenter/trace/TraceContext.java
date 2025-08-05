package com.example.configcenter.trace;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链路追踪上下文
 */
public class TraceContext {
    private static final ThreadLocal<TraceContext> context = new ThreadLocal<>();
    
    private String traceId;           // 全局唯一标识
    private String spanId;            // 当前操作标识
    private String parentSpanId;      // 父操作标识
    private String userEmail;         // 用户标识
    private String requestId;         // 请求标识
    private long startTime;           // 开始时间
    private Map<String, String> tags; // 标签信息
    
    public TraceContext() {
        this.tags = new ConcurrentHashMap<>();
    }
    
    // 生成TraceId
    public static String generateTraceId() {
        return "trace_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    // 生成SpanId
    public static String generateSpanId() {
        return "span_" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    // 获取当前上下文
    public static TraceContext getContext() {
        return context.get();
    }
    
    // 设置当前上下文
    public static void setContext(TraceContext traceContext) {
        context.set(traceContext);
    }
    
    // 清除当前上下文
    public static void clearContext() {
        context.remove();
    }
    
    // 添加标签
    public void addTag(String key, String value) {
        this.tags.put(key, value);
    }
    
    // 获取标签
    public String getTag(String key) {
        return this.tags.get(key);
    }
    
    // Getter和Setter方法
    public String getTraceId() {
        return traceId;
    }
    
    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
    
    public String getSpanId() {
        return spanId;
    }
    
    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }
    
    public String getParentSpanId() {
        return parentSpanId;
    }
    
    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
} 