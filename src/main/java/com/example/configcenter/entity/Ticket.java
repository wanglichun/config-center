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
    private String createTime;
    private Long lastUpdateTime;
    private String lastAuthor;
    private String oldData;
    private String newData;
}
