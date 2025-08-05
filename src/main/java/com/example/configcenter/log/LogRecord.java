package com.example.configcenter.log;

import lombok.Data;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 统一日志记录实体
 */
@Data
public class LogRecord {
    private String timestamp;           // 时间戳
    private String level;              // 日志级别
    private String category;           // 日志分类 (API_REQUEST, ZOOKEEPER, REDIS, MYSQL)
    private String traceId;            // 链路追踪ID
    private String spanId;             // 当前Span ID
    private String parentSpanId;       // 父Span ID
    private String operation;          // 操作类型
    private long startTime;            // 开始时间
    private long endTime;              // 结束时间
    private long duration;             // 耗时
    private Map<String, Object> data; // 详细数据
    private Map<String, String> tags; // 标签信息
    private String errorMessage;       // 错误信息
    private String stackTrace;         // 堆栈信息
    
    public LogRecord() {
        this.data = new ConcurrentHashMap<>();
        this.tags = new ConcurrentHashMap<>();
    }
} 