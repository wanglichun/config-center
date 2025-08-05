package com.example.configcenter.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka配置类
 */
@Configuration
public class KafkaConfig {
    
    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "1");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
    
    @Bean
    public NewTopic apiLogsTopic() {
        return TopicBuilder.name("api-logs")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic redisLogsTopic() {
        return TopicBuilder.name("redis-logs")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic mysqlLogsTopic() {
        return TopicBuilder.name("mysql-logs")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic zkLogsTopic() {
        return TopicBuilder.name("zk-logs")
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic traceLogsTopic() {
        return TopicBuilder.name("trace-logs")
                .partitions(3)
                .replicas(1)
                .build();
    }
} 