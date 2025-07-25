package com.example.configcenter.enums;

/**
 * 工单阶段枚举
 */
public enum TicketPhaseEnum {
    
    /**
     * 待处理
     */
    PENDING("PENDING", "待处理"),
    
    /**
     * 处理中
     */
    PROCESSING("PROCESSING", "处理中"),
    
    /**
     * 已批准
     */
    APPROVED("APPROVED", "已批准"),
    
    /**
     * 已拒绝
     */
    REJECTED("REJECTED", "已拒绝"),
    
    /**
     * 已完成
     */
    COMPLETED("COMPLETED", "已完成");
    
    private final String code;
    private final String description;
    
    TicketPhaseEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static TicketPhaseEnum getByCode(String code) {
        for (TicketPhaseEnum phase : values()) {
            if (phase.getCode().equals(code)) {
                return phase;
            }
        }
        return null;
    }
} 