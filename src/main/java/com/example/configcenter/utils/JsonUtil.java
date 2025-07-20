package com.example.configcenter.utils;

import com.example.configcenter.service.impl.MachineConfigSubscriptionServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();


    /**
     * 安全地将JSON字符串转换为指定类型的对象
     * @param <T> 目标对象类型
     * @param json 源JSON字符串
     * @param targetClass 目标对象的Class
     * @return 转换后的对象实例
     * @throws IllegalArgumentException 如果JSON格式无效或转换失败
     */
    public static <T> T jsonToObject(String json, Class<T> targetClass) {
        // 参数校验
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        try {
            // 执行转换
            return mapper.readValue(json, targetClass);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON转换失败: " + json, e);
        }
    }

    public static <T> String objectToString(Object object) {
        // 参数校验
        if (object == null) {
            return "";
        }

        try {
            // 执行转换
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON转换失败: " + object, e);
        }
    }
}

