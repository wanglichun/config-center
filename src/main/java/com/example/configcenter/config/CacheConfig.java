package com.example.configcenter.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 缓存配置类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // 不同缓存区域的配置
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 单个配置项缓存 - 1小时过期
        cacheConfigurations.put("config", defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 配置列表缓存 - 30分钟过期
        cacheConfigurations.put("configs", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 配置映射缓存 - 2小时过期
        cacheConfigurations.put("configMap", defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 用户相关缓存 - 15分钟过期
        cacheConfigurations.put("user", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 工单相关缓存 - 10分钟过期
        cacheConfigurations.put("ticket", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // 重要配置缓存 - 4小时过期
        cacheConfigurations.put("critical_config", defaultConfig.entryTtl(Duration.ofHours(4)));
        
        // 临时配置缓存 - 5分钟过期
        cacheConfigurations.put("temp_config", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
} 