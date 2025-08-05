package com.example.configcenter.dto;

import com.example.configcenter.common.PageBase;
import lombok.Data;

/**
 * 工单查询请求DTO
 */
@Data
public class TicketQueryRequest extends PageBase {
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
     * 配置id
     */
    private Long configId;
} 