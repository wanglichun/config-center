package com.example.configcenter.enums;

public enum ConfigStatusEnum {

    Init("Init"),
    Online("Online"),
    Offline("Offline"),
    Deleted("Deleted");



    private final String value;

    ConfigStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
