package com.example.configcenter.enums;

/**
 * 灰度发布相关枚举
 */
public class GrayReleaseEnum {
    
    /**
     * 灰度发布策略
     */
    public enum Strategy {
        IP_WHITELIST("IP_WHITELIST", "IP白名单"),
        USER_WHITELIST("USER_WHITELIST", "用户白名单"),
        PERCENTAGE("PERCENTAGE", "按比例发布"),
        CANARY("CANARY", "金丝雀发布");
        
        private final String code;
        private final String description;
        
        Strategy(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 灰度发布状态
     */
    public enum Status {
        DRAFT("DRAFT", "草稿"),
        ACTIVE("ACTIVE", "进行中"),
        PAUSED("PAUSED", "已暂停"),
        COMPLETED("COMPLETED", "已完成"),
        FAILED("FAILED", "失败"),
        ROLLBACK("ROLLBACK", "已回滚");
        
        private final String code;
        private final String description;
        
        Status(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 灰度发布详情状态
     */
    public enum DetailStatus {
        PENDING("PENDING", "待发布"),
        GRAY("GRAY", "灰度中"),
        PUBLISHED("PUBLISHED", "已发布"),
        ROLLBACK("ROLLBACK", "已回滚");
        
        private final String code;
        private final String description;
        
        DetailStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 灰度发布操作类型
     */
    public enum OperationType {
        CREATE("CREATE", "创建"),
        START("START", "开始"),
        PAUSE("PAUSE", "暂停"),
        RESUME("RESUME", "恢复"),
        COMPLETE("COMPLETE", "完成"),
        ROLLBACK("ROLLBACK", "回滚"),
        CANCEL("CANCEL", "取消");
        
        private final String code;
        private final String description;
        
        OperationType(String code, String description) {
            this.code = code;
            this.description = description;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 