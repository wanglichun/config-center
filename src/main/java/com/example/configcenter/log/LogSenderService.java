package com.example.configcenter.log;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 日志发送服务
 */
@Slf4j
@Service
public class LogSenderService {
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 发送API请求日志
     */
    public void sendApiLog(String traceId, String spanId, String parentSpanId, 
                          String method, String url, int statusCode, long duration,
                          String clientIp, String userId, String errorMessage) {
        
        LogRecord logRecord = new LogRecord();
        logRecord.setTimestamp(LocalDateTime.now().format(FORMATTER));
        logRecord.setLevel("INFO");
        logRecord.setCategory("API_REQUEST");
        logRecord.setTraceId(traceId);
        logRecord.setSpanId(spanId);
        logRecord.setParentSpanId(parentSpanId);
        logRecord.setOperation(method);
        logRecord.setStartTime(System.currentTimeMillis() - duration);
        logRecord.setEndTime(System.currentTimeMillis());
        logRecord.setDuration(duration);
        
        // 设置详细数据
        logRecord.getData().put("method", method);
        logRecord.getData().put("url", url);
        logRecord.getData().put("statusCode", statusCode);
        logRecord.getData().put("clientIp", clientIp);
        logRecord.getData().put("userId", userId);
        
        if (errorMessage != null) {
            logRecord.setErrorMessage(errorMessage);
            logRecord.setLevel("ERROR");
        }
        
        sendToKafkaAsyncVoid("api-logs", logRecord);
    }
    
    /**
     * 发送Redis操作日志
     */
    public void sendRedisLog(String traceId, String spanId, String parentSpanId,
                           String operation, String key, Object value, long duration,
                           boolean hit, String errorMessage) {
        
        LogRecord logRecord = new LogRecord();
        logRecord.setTimestamp(LocalDateTime.now().format(FORMATTER));
        logRecord.setLevel("INFO");
        logRecord.setCategory("REDIS");
        logRecord.setTraceId(traceId);
        logRecord.setSpanId(spanId);
        logRecord.setParentSpanId(parentSpanId);
        logRecord.setOperation(operation);
        logRecord.setStartTime(System.currentTimeMillis() - duration);
        logRecord.setEndTime(System.currentTimeMillis());
        logRecord.setDuration(duration);
        
        // 设置详细数据
        logRecord.getData().put("operation", operation);
        logRecord.getData().put("key", key);
        logRecord.getData().put("value", value);
        logRecord.getData().put("hit", hit);
        
        if (errorMessage != null) {
            logRecord.setErrorMessage(errorMessage);
            logRecord.setLevel("ERROR");
        }
        
        sendToKafkaAsyncVoid("redis-logs", logRecord);
    }
    
    /**
     * 发送MySQL操作日志
     */
    public void sendMysqlLog(String traceId, String spanId, String parentSpanId,
                           String operation, String sql, Object result, long duration,
                           int rowsAffected, String errorMessage) {
        
        LogRecord logRecord = new LogRecord();
        logRecord.setTimestamp(LocalDateTime.now().format(FORMATTER));
        logRecord.setLevel("INFO");
        logRecord.setCategory("MYSQL");
        logRecord.setTraceId(traceId);
        logRecord.setSpanId(spanId);
        logRecord.setParentSpanId(parentSpanId);
        logRecord.setOperation(operation);
        logRecord.setStartTime(System.currentTimeMillis() - duration);
        logRecord.setEndTime(System.currentTimeMillis());
        logRecord.setDuration(duration);
        
        // 设置详细数据
        logRecord.getData().put("operation", operation);
        logRecord.getData().put("sql", sql);
        logRecord.getData().put("result", result);
        logRecord.getData().put("rowsAffected", rowsAffected);
        
        if (errorMessage != null) {
            logRecord.setErrorMessage(errorMessage);
            logRecord.setLevel("ERROR");
        }
        
        sendToKafkaAsyncVoid("mysql-logs", logRecord);
    }
    
    /**
     * 发送ZooKeeper操作日志
     */
    public void sendZkLog(String traceId, String spanId, String parentSpanId,
                         String operation, String path, Object data, long duration,
                         String errorMessage) {
        
        LogRecord logRecord = new LogRecord();
        logRecord.setTimestamp(LocalDateTime.now().format(FORMATTER));
        logRecord.setLevel("INFO");
        logRecord.setCategory("ZOOKEEPER");
        logRecord.setTraceId(traceId);
        logRecord.setSpanId(spanId);
        logRecord.setParentSpanId(parentSpanId);
        logRecord.setOperation(operation);
        logRecord.setStartTime(System.currentTimeMillis() - duration);
        logRecord.setEndTime(System.currentTimeMillis());
        logRecord.setDuration(duration);
        
        // 设置详细数据
        logRecord.getData().put("operation", operation);
        logRecord.getData().put("path", path);
        logRecord.getData().put("data", data);
        
        if (errorMessage != null) {
            logRecord.setErrorMessage(errorMessage);
            logRecord.setLevel("ERROR");
        }
        
        sendToKafkaAsyncVoid("zk-logs", logRecord);
    }
    
    /**
     * 发送链路追踪日志
     */
    public void sendTraceLog(String traceId, String spanId, String parentSpanId,
                           String operation, String component, long duration,
                           Map<String, Object> data, String errorMessage) {
        
        LogRecord logRecord = new LogRecord();
        logRecord.setTimestamp(LocalDateTime.now().format(FORMATTER));
        logRecord.setLevel("INFO");
        logRecord.setCategory("TRACE");
        logRecord.setTraceId(traceId);
        logRecord.setSpanId(spanId);
        logRecord.setParentSpanId(parentSpanId);
        logRecord.setOperation(operation);
        logRecord.setStartTime(System.currentTimeMillis() - duration);
        logRecord.setEndTime(System.currentTimeMillis());
        logRecord.setDuration(duration);
        logRecord.setData(data);
        
        if (errorMessage != null) {
            logRecord.setErrorMessage(errorMessage);
            logRecord.setLevel("ERROR");
        }
        
        sendToKafkaAsyncVoid("trace-logs", logRecord);
    }

    /**
     * 异步发送日志到Kafka
     */
    @Async("logSenderExecutor")
    public CompletableFuture<Void> sendToKafkaAsync(String topic, LogRecord logRecord) {
        return CompletableFuture.runAsync(() -> {
            try {
                String message = JSON.toJSONString(logRecord);
                kafkaTemplate.send(topic, message);
                log.debug("异步发送日志到Kafka成功: topic={}, traceId={}", topic, logRecord.getTraceId());
            } catch (Exception e) {
                log.error("异步发送日志到Kafka失败: topic={}, traceId={}", topic, logRecord.getTraceId(), e);
            }
        });
    }
    
    /**
     * 异步发送日志到Kafka（无返回值）
     */
    @Async("logSenderExecutor")
    public void sendToKafkaAsyncVoid(String topic, LogRecord logRecord) {
        try {
            String message = JSON.toJSONString(logRecord);
            kafkaTemplate.send(topic, message);
            log.debug("异步发送日志到Kafka成功: topic={}, traceId={}", topic, logRecord.getTraceId());
        } catch (Exception e) {
            log.error("异步发送日志到Kafka失败: topic={}, traceId={}", topic, logRecord.getTraceId(), e);
        }
    }
} 