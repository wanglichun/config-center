package com.example.configcenter.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单实体类
 */
@Data
public class Ticket {
    private Long id;

    /**
     * 关联数据ID
     */
    private Long dataId;
    
    /**
     * 工单标题
     */
    private String title;
    
    /**
     * 工单阶段
     */
    private String phase;
    
    /**
     * 申请人
     */
    private String applicator;
    
    /**
     * 操作人
     */
    private String operator;
    
    /**
     * 旧数据(JSON格式)
     */
    private String oldData;
    
    /**
     * 新数据(JSON格式)
     */
    private String newData;

    private long createTime;

    private long updateTime;
}
