package com.example.configcenter.utils;

import com.example.configcenter.service.enums.EnumService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EnumUtils {

    // 将多个枚举类的映射合并到一个Map中
    public static Map<String, Map<?, ?>> getAllEnumMaps(Class<?>... enumClasses) {
        Map<String, Map<?, ?>> result = new HashMap<>();
        for (Class<?> enumClass : enumClasses) {
            if (!enumClass.isEnum() || !EnumService.class.isAssignableFrom(enumClass)) {
                continue; // 跳过非枚举或未实现接口的类
            }
            String enumName = enumClass.getSimpleName();
            Map<?, ?> enumMap = createEnumMap(enumClass);
            result.put(enumName, enumMap);
        }
        return result;
    }

    // 创建单个枚举的映射（code -> description）
    @SuppressWarnings("unchecked")
    private static <T> Map<T, String> createEnumMap(Class<?> enumClass) {
        Class<? extends EnumService<T>> genericEnumClass =
                (Class<? extends EnumService<T>>) enumClass;

        return Arrays.stream(genericEnumClass.getEnumConstants())
                .collect(Collectors.toMap(
                        EnumService::getCode,
                        EnumService::getDescription
                ));
    }
}
