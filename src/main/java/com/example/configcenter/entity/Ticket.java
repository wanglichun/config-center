package com.example.configcenter.entity;

import lombok.Data;

@Data
public class Ticket {
    private Integer id;
    private Integer dataId;
    private String title;
    private String phase;
    private String applicator;
    private String operator;
    private Long createTime;
    private Long updateTime;
    private String oldData;
    private String newData;
}
