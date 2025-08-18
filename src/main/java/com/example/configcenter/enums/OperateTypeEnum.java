package com.example.configcenter.enums;

public enum OperateTypeEnum {

    Edit("Edit"),
    Online("Online"),
    Offline("Offline"),
    Delete("Delete");



    private final String value;

    OperateTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
