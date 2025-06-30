package com.example.configcenter.enums;

import com.example.configcenter.service.enums.EnumService;

public enum ConfigStatusEnum implements EnumService<String> {

    DRAFT("DRAFT", "草稿"),
    PUBLISHED("PUBLISHED", "已发布"),
    DISABLED("DISABLED", "已禁用");

    private final String code;
    private final String description;

    ConfigStatusEnum(String code, String description) {
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
