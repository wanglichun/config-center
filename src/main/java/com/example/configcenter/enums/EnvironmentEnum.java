package com.example.configcenter.enums;

import com.example.configcenter.service.enums.EnumService;

public enum EnvironmentEnum implements EnumService<String> {

    DEV("dev", "开发环境"),
    TEST("test", "测试环境"),
    PROD("prod", "生产环境");

    private final String code;
    private final String description;

    EnvironmentEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
