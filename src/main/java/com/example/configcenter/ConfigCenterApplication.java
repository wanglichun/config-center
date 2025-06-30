package com.example.configcenter;

import com.example.configcenter.enums.ConfigStatusEnum;
import com.example.configcenter.enums.EnvironmentEnum;
import com.example.configcenter.utils.EnumUtils;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Map;

/**
 * 配置中心启动类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@MapperScan("com.example.configcenter.mapper")
public class ConfigCenterApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigCenterApplication.class, args);
        System.out.println("========================================");
        System.out.println("  配置中心启动成功！");
        System.out.println("  访问地址: http://localhost:8080/config-center");
        System.out.println("  Swagger文档: http://localhost:8080/config-center/swagger-ui.html");
        System.out.println("  监控面板: http://localhost:8080/config-center/actuator");
        System.out.println("========================================");

        Map<String, Map<?, ?>> allEnums = EnumUtils.getAllEnumMaps(
                EnvironmentEnum.class,
                ConfigStatusEnum.class
        );

        // 打印结果
        System.out.println(allEnums);
    }
} 