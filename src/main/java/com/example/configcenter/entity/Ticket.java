package com.example.configcenter.entity;

import lombok.Data;

/**
 * 工单实体类
 */
@Data
public class Ticket {
    
    /**
     * 工单ID
     */
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
     * 创建时间戳
     */
    private Long createTime;
    
    /**
     * 更新时间戳
     */
    private Long updateTime;
    
    /**
     * 旧数据(JSON格式)
     */
    private String oldData;
    
    /**
     * 新数据(JSON格式)
     */
    private String newData;
}
