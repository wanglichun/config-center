package com.example.configcenter.dto;

import lombok.Data;

@Data
public class TicketCreateDto {
    /**
     * 关联数据ID
     */
    private Long dataId;

    /**
     * 工单标题
     */
    private String title;

    /**
     * 新数据(JSON格式)
     */
    private String newData;
}
