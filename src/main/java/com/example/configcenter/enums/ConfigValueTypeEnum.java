package com.example.configcenter.enums;

public enum ConfigValueTypeEnum {

    STRING("STRING"),
    JSON("JSON"),
    YAML("YAML");


    private final String value;

    ConfigValueTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
