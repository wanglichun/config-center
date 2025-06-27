package com.example.configcenter.enums;

import lombok.Data;

import java.util.List;

public class EnumValue {
    String enumName;
    List<EnumValuePair> enums;

    @Data
    public static class EnumValuePair {
        Integer id;
        String value;
        String displayName;
    }
}
