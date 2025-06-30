package com.example.configcenter.enums;

import com.example.configcenter.service.enums.EnumService;

public enum EnvironmentEnum implements EnumService<String> {

    DEV("DEV", "DEV"),
    TEST("TEST", "TEST"),
    LIVE("LIVE", "LIVE");

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
