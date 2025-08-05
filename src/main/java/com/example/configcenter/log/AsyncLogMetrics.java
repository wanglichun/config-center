package com.example.configcenter.log;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 异步日志发送监控指标
 */
@Component
@Data
public class AsyncLogMetrics {
    
    // 发送成功计数
    private final AtomicLong successCount = new AtomicLong(0);
    
    // 发送失败计数
    private final AtomicLong failureCount = new AtomicLong(0);
    
    // 队列大小
    private final AtomicLong queueSize = new AtomicLong(0);
    
    // 平均发送时间（毫秒）
    private final AtomicLong totalSendTime = new AtomicLong(0);
    private final AtomicLong sendCount = new AtomicLong(0);
    
    /**
     * 记录发送成功
     */
    public void recordSuccess() {
        successCount.incrementAndGet();
    }
    
    /**
     * 记录发送失败
     */
    public void recordFailure() {
        failureCount.incrementAndGet();
    }
    
    /**
     * 记录发送时间
     */
    public void recordSendTime(long sendTime) {
        totalSendTime.addAndGet(sendTime);
        sendCount.incrementAndGet();
    }
    
    /**
     * 更新队列大小
     */
    public void updateQueueSize(long size) {
        queueSize.set(size);
    }
    
    /**
     * 获取平均发送时间
     */
    public double getAverageSendTime() {
        long count = sendCount.get();
        if (count == 0) {
            return 0.0;
        }
        return (double) totalSendTime.get() / count;
    }
    
    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        long total = successCount.get() + failureCount.get();
        if (total == 0) {
            return 1.0;
        }
        return (double) successCount.get() / total;
    }
    
    /**
     * 重置指标
     */
    public void reset() {
        successCount.set(0);
        failureCount.set(0);
        queueSize.set(0);
        totalSendTime.set(0);
        sendCount.set(0);
    }
} 