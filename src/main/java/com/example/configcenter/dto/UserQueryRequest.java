package com.example.configcenter.dto;

import com.example.configcenter.common.PageBase;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户查询请求对象
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageBase {
    
    /**
     * 关键词（用户名、姓名、邮箱）
     */
    private String keyword;
    
    /**
     * 角色过滤
     */
    private String role;
    
    /**
     * 状态过滤
     */
    private String status;
    
    /**
     * 部门过滤
     */
    private String department;
    
    /**
     * 开始创建时间
     */
    private String startTime;
    
    /**
     * 结束创建时间
     */
    private String endTime;
} 