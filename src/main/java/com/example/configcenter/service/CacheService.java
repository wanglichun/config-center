package com.example.configcenter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 自定义缓存服务
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置缓存，根据key模式确定过期时间
     */
    public void setCache(String key, Object value) {
        Duration ttl = getTtlByKey(key);
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 获取缓存
     */
    public Object getCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除缓存
     */
    public void deleteCache(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 根据key模式确定过期时间
     */
    private Duration getTtlByKey(String key) {
        if (key.startsWith("critical_config::")) {
            return Duration.ofHours(4); // 重要配置4小时
        } else if (key.startsWith("temp_config::")) {
            return Duration.ofMinutes(5); // 临时配置5分钟
        } else if (key.startsWith("user::")) {
            return Duration.ofMinutes(15); // 用户信息15分钟
        } else if (key.startsWith("ticket::")) {
            return Duration.ofMinutes(10); // 工单信息10分钟
        } else if (key.startsWith("configs::")) {
            return Duration.ofMinutes(30); // 配置列表30分钟
        } else if (key.startsWith("configMap::")) {
            return Duration.ofHours(2); // 配置映射2小时
        } else {
            return Duration.ofHours(1); // 默认1小时
        }
    }

    /**
     * 设置自定义过期时间
     */
    public void setCacheWithTtl(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 检查缓存是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 获取缓存剩余时间
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }
} 