package com.example.configcenter.dto;

import lombok.Data;

/**
 * 工单查询请求DTO
 */
@Data
public class TicketQueryRequest {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
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
} 