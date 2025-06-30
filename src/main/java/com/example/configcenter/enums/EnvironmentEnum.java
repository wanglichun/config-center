package com.example.configcenter.enums;

public enum EnvironmentEnum {

    TEST("TEST"),
    LIVE("LIVE");

    private String value;

    EnvironmentEnum(String value) {
        this.value = value;
    }
}
