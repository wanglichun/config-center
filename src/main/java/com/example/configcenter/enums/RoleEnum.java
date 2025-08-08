package com.example.configcenter.enums;

public enum RoleEnum {

    Admin("Admin"),
    Developer("Developer");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
