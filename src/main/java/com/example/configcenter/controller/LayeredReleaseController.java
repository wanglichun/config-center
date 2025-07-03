package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.service.MachineGroupService;
import com.example.configcenter.service.MachineGroupService.LayeredMachineGroups;
import com.example.configcenter.service.MachineGroupService.MachineInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 分层发布控制器
 */
@RestController
@RequestMapping("/api/layered-release")
@Validated
public class LayeredReleaseController {
    
    private static final Logger log = LoggerFactory.getLogger(LayeredReleaseController.class);
    
    @Autowired
    private MachineGroupService machineGroupService;
    
    /**
     * 预览机器分层
     */
    @PostMapping("/preview")
    public ApiResult<LayeredMachineGroups> previewMachineGroups(@RequestBody PreviewRequest request) {
        try {
            log.info("预览机器分层: appName={}, environment={}, configKeys={}", 
                    request.getAppName(), request.getEnvironment(), request.getConfigKeys());
            
            // 获取订阅配置的机器列表
            List<MachineInstance> machines = machineGroupService.getSubscribedMachines(
                    request.getAppName(), request.getEnvironment(), request.getConfigKeys());
            
            if (machines.isEmpty()) {
                return ApiResult.error("未找到订阅该配置的机器");
            }
            
            // 分层
            LayeredMachineGroups groups = machineGroupService.splitMachinesIntoLayers(machines);
            
            log.info("机器分层预览完成: 总数={}, 第一层={}, 第二层={}, 第三层={}", 
                    groups.getTotalCount(), groups.getLayer1().size(), 
                    groups.getLayer2().size(), groups.getLayer3().size());
            
            return ApiResult.success(groups);
            
        } catch (Exception e) {
            log.error("预览机器分层失败", e);
            return ApiResult.error("预览机器分层失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置订阅的机器列表
     */
    @GetMapping("/machines")
    public ApiResult<List<MachineInstance>> getSubscribedMachines(
            @RequestParam @NotBlank String appName,
            @RequestParam @NotBlank String environment,
            @RequestParam @NotNull List<String> configKeys) {
        try {
            log.info("获取配置订阅机器: appName={}, environment={}, configKeys={}", 
                    appName, environment, configKeys);
            
            List<MachineInstance> machines = machineGroupService.getSubscribedMachines(
                    appName, environment, configKeys);
            
            return ApiResult.success(machines);
            
        } catch (Exception e) {
            log.error("获取配置订阅机器失败", e);
            return ApiResult.error("获取配置订阅机器失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取机器分组信息
     */
    @GetMapping("/groups")
    public ApiResult<Map<String, List<Map<String, Object>>>> getMachineGroups(
            @RequestParam @NotBlank String appName,
            @RequestParam @NotBlank String environment) {
        try {
            log.info("获取机器分组: appName={}, environment={}", appName, environment);
            
            Map<String, List<Map<String, Object>>> groups = machineGroupService.getAllMachinesByGroups(
                    appName, environment);
            
            return ApiResult.success(groups);
            
        } catch (Exception e) {
            log.error("获取机器分组失败", e);
            return ApiResult.error("获取机器分组失败: " + e.getMessage());
        }
    }
    
    /**
     * 注册机器到分组
     */
    @PostMapping("/machines/register")
    public ApiResult<Void> registerMachine(@RequestBody RegisterMachineRequest request,
                                          Authentication authentication) {
        try {
            log.info("注册机器: appName={}, environment={}, groupName={}, instanceId={}", 
                    request.getAppName(), request.getEnvironment(), request.getGroupName(), 
                    request.getInstanceId());
            
            machineGroupService.registerMachine(request.getAppName(), request.getEnvironment(),
                    request.getGroupName(), request.getInstanceId(), request.getInstanceInfo());
            
            return ApiResult.success();
            
        } catch (Exception e) {
            log.error("注册机器失败", e);
            return ApiResult.error("注册机器失败: " + e.getMessage());
        }
    }
    
    /**
     * 从分组中注销机器
     */
    @PostMapping("/machines/unregister")
    public ApiResult<Void> unregisterMachine(@RequestBody UnregisterMachineRequest request,
                                            Authentication authentication) {
        try {
            log.info("注销机器: appName={}, environment={}, groupName={}, instanceId={}", 
                    request.getAppName(), request.getEnvironment(), request.getGroupName(), 
                    request.getInstanceId());
            
            machineGroupService.unregisterMachine(request.getAppName(), request.getEnvironment(),
                    request.getGroupName(), request.getInstanceId());
            
            return ApiResult.success();
            
        } catch (Exception e) {
            log.error("注销机器失败", e);
            return ApiResult.error("注销机器失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建分组
     */
    @PostMapping("/groups")
    public ApiResult<Void> createGroup(@RequestBody CreateGroupRequest request,
                                      Authentication authentication) {
        try {
            log.info("创建分组: appName={}, environment={}, groupName={}", 
                    request.getAppName(), request.getEnvironment(), request.getGroupName());
            
            machineGroupService.createGroup(request.getAppName(), request.getEnvironment(),
                    request.getGroupName(), request.getDescription());
            
            return ApiResult.success();
            
        } catch (Exception e) {
            log.error("创建分组失败", e);
            return ApiResult.error("创建分组失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取机器健康状态
     */
    @GetMapping("/machines/{instanceId}/health")
    public ApiResult<Boolean> getMachineHealth(@PathVariable String instanceId) {
        try {
            boolean isHealthy = machineGroupService.isHealthy(instanceId);
            return ApiResult.success(isHealthy);
            
        } catch (Exception e) {
            log.error("获取机器健康状态失败: {}", instanceId, e);
            return ApiResult.error("获取机器健康状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取机器稳定性评分
     */
    @GetMapping("/machines/{instanceId}/stability")
    public ApiResult<Double> getMachineStability(@PathVariable String instanceId) {
        try {
            double score = machineGroupService.getMachineStabilityScore(instanceId);
            return ApiResult.success(score);
            
        } catch (Exception e) {
            log.error("获取机器稳定性评分失败: {}", instanceId, e);
            return ApiResult.error("获取机器稳定性评分失败: " + e.getMessage());
        }
    }
    
    // 请求/响应对象
    
    public static class PreviewRequest {
        @NotBlank(message = "应用名称不能为空")
        private String appName;
        
        @NotBlank(message = "环境不能为空")
        private String environment;
        
        @NotNull(message = "配置键列表不能为空")
        private List<String> configKeys;
        
        // Getters and Setters
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        
        public List<String> getConfigKeys() { return configKeys; }
        public void setConfigKeys(List<String> configKeys) { this.configKeys = configKeys; }
    }
    
    public static class RegisterMachineRequest {
        @NotBlank(message = "应用名称不能为空")
        private String appName;
        
        @NotBlank(message = "环境不能为空")
        private String environment;
        
        @NotBlank(message = "分组名称不能为空")
        private String groupName;
        
        @NotBlank(message = "实例ID不能为空")
        private String instanceId;
        
        @NotNull(message = "实例信息不能为空")
        private Map<String, Object> instanceInfo;
        
        // Getters and Setters
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
        
        public Map<String, Object> getInstanceInfo() { return instanceInfo; }
        public void setInstanceInfo(Map<String, Object> instanceInfo) { this.instanceInfo = instanceInfo; }
    }
    
    public static class UnregisterMachineRequest {
        @NotBlank(message = "应用名称不能为空")
        private String appName;
        
        @NotBlank(message = "环境不能为空")
        private String environment;
        
        @NotBlank(message = "分组名称不能为空")
        private String groupName;
        
        @NotBlank(message = "实例ID不能为空")
        private String instanceId;
        
        // Getters and Setters
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public String getInstanceId() { return instanceId; }
        public void setInstanceId(String instanceId) { this.instanceId = instanceId; }
    }
    
    public static class CreateGroupRequest {
        @NotBlank(message = "应用名称不能为空")
        private String appName;
        
        @NotBlank(message = "环境不能为空")
        private String environment;
        
        @NotBlank(message = "分组名称不能为空")
        private String groupName;
        
        private String description;
        
        // Getters and Setters
        public String getAppName() { return appName; }
        public void setAppName(String appName) { this.appName = appName; }
        
        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }
        
        public String getGroupName() { return groupName; }
        public void setGroupName(String groupName) { this.groupName = groupName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
} 