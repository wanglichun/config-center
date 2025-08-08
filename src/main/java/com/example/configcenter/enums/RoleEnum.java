package com.example.configcenter.enums;

public enum RoleEnum {

    ADMIN("ADMIN"),
    DEVELOPER("DEVELOPER"),

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
