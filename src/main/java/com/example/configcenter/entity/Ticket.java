package com.example.configcenter.entity;

import com.example.configcenter.enums.TicketActionEnum;
import com.example.configcenter.enums.TicketPhaseEnum;
import lombok.Data;

import java.util.List;

/**
 * 工单实体类
 */
@Data
public class Ticket {

    /**
     * Ticket ID
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
    private TicketPhaseEnum phase;

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

    /**
     * 创建时间
     */
    private long createTime;

    /**
     * 更新时间
     */
    private long updateTime;

    /**
     * 行为列表
     */
    private List<TicketActionEnum> action;

    public List<TicketActionEnum> getAction() {
        switch (phase) {
            case Reviewing:
                return List.of(TicketActionEnum.Approve, TicketActionEnum.Reject, TicketActionEnum.Cancel);
            case GrayPublish:
                return List.of(TicketActionEnum.Publish, TicketActionEnum.Cancel);
            default:
                return List.of();
        }
    }
}
