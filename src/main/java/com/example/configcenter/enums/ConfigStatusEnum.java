package com.example.configcenter.enums;

public enum ConfigStatusEnum {

    DRAFT("DRAFT"),
    OFFLINE("OFFLINE"),
    ONLINE("ONLINE"),
    DELETE("DELETE");

    private String value;

    ConfigStatusEnum(String value) {
        this.value = value;
    }
}
