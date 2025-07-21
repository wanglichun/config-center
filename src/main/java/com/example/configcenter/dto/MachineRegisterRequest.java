package com.example.configcenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 机器注册请求DTO
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class MachineRegisterRequest {
    
    /**
     * 配置组名称
     */
    @NotBlank(message = "配置组名称不能为空")
    @Size(max = 100, message = "配置组名称长度不能超过100字符")
    private String groupName;
    
    /**
     * 实例ID
     */
    @NotBlank(message = "实例ID不能为空")
    @Size(max = 200, message = "实例ID长度不能超过200字符")
    private String instanceId;
    
    /**
     * 实例IP地址
     */
    @NotBlank(message = "实例IP地址不能为空")
    @Size(max = 50, message = "实例IP地址长度不能超过50字符")
    private String instanceIp;
    
    /**
     * 订阅的配置键列表
     */
    @NotEmpty(message = "订阅的配置键列表不能为空")
    private List<String> configKeys;
    
    /**
     * 实例描述（可选）
     */
    @Size(max = 500, message = "实例描述长度不能超过500字符")
    private String description;
    
    /**
     * 实例标签（可选）
     */
    @Size(max = 200, message = "实例标签长度不能超过200字符")
    private String tags;
} 