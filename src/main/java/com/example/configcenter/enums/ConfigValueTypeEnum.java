package com.example.configcenter.enums;

public enum ConfigValueTypeEnum {

    Json("Json"),
    Yaml("Yaml");


    private final String value;

    ConfigValueTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
