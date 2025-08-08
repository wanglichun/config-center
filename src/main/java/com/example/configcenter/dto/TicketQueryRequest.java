package com.example.configcenter.dto;

import com.example.configcenter.common.PageBase;
import com.example.configcenter.enums.TicketPhaseEnum;
import lombok.Data;

import java.util.List;

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
    private List<TicketPhaseEnum> phase;
    
    /**
     * 申请人
     */
    private String applicator;

    /**
     * 配置id
     */
    private Long configId;
} 