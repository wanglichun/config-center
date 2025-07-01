package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.service.MachineGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 机器分组管理控制器
 */
@RestController
@RequestMapping("/api/machine-groups")
public class MachineGroupController {
    
    private static final Logger log = LoggerFactory.getLogger(MachineGroupController.class);
    
    @Autowired
    private MachineGroupService machineGroupService;
    
    /**
     * 获取应用的所有分组
     */
    @GetMapping("/groups")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<List<String>> getGroups(@RequestParam String appName,
                                            @RequestParam String environment) {
        try {
            List<String> groups = machineGroupService.getGroups(appName, environment);
            return ApiResult.success(groups);
        } catch (Exception e) {
            log.error("获取应用分组失败，appName: {}, environment: {}", appName, environment, e);
            return ApiResult.error("获取应用分组失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取指定分组的机器列表
     */
    @GetMapping("/machines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<List<Map<String, Object>>> getMachinesByGroup(@RequestParam String appName,
                                                                  @RequestParam String environment,
                                                                  @RequestParam String groupName) {
        try {
            List<Map<String, Object>> machines = machineGroupService.getMachinesByGroup(appName, environment, groupName);
            return ApiResult.success(machines);
        } catch (Exception e) {
            log.error("获取分组机器失败，appName: {}, environment: {}, groupName: {}", appName, environment, groupName, e);
            return ApiResult.error("获取分组机器失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取应用的所有机器（按分组组织）
     */
    @GetMapping("/all-machines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Map<String, List<Map<String, Object>>>> getAllMachinesByGroups(@RequestParam String appName,
                                                                                    @RequestParam String environment) {
        try {
            Map<String, List<Map<String, Object>>> machines = machineGroupService.getAllMachinesByGroups(appName, environment);
            return ApiResult.success(machines);
        } catch (Exception e) {
            log.error("获取应用所有机器分组失败，appName: {}, environment: {}", appName, environment, e);
            return ApiResult.error("获取应用所有机器分组失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建机器分组
     */
    @PostMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> createGroup(@RequestParam String appName,
                                      @RequestParam String environment,
                                      @RequestParam String groupName,
                                      @RequestParam(required = false) String description) {
        try {
            machineGroupService.createGroup(appName, environment, groupName, description);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("创建机器分组失败，appName: {}, environment: {}, groupName: {}", appName, environment, groupName, e);
            return ApiResult.error("创建机器分组失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除机器分组
     */
    @DeleteMapping("/groups")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResult<Void> deleteGroup(@RequestParam String appName,
                                      @RequestParam String environment,
                                      @RequestParam String groupName) {
        try {
            machineGroupService.deleteGroup(appName, environment, groupName);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("删除机器分组失败，appName: {}, environment: {}, groupName: {}", appName, environment, groupName, e);
            return ApiResult.error("删除机器分组失败: " + e.getMessage());
        }
    }
    
    /**
     * 注册机器到分组
     */
    @PostMapping("/machines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Void> registerMachine(@RequestParam String appName,
                                          @RequestParam String environment,
                                          @RequestParam String groupName,
                                          @RequestParam String instanceId,
                                          @RequestBody Map<String, Object> instanceInfo) {
        try {
            machineGroupService.registerMachine(appName, environment, groupName, instanceId, instanceInfo);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("注册机器失败，appName: {}, environment: {}, groupName: {}, instanceId: {}", 
                     appName, environment, groupName, instanceId, e);
            return ApiResult.error("注册机器失败: " + e.getMessage());
        }
    }
    
    /**
     * 从分组中移除机器
     */
    @DeleteMapping("/machines")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Void> unregisterMachine(@RequestParam String appName,
                                            @RequestParam String environment,
                                            @RequestParam String groupName,
                                            @RequestParam String instanceId) {
        try {
            machineGroupService.unregisterMachine(appName, environment, groupName, instanceId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("注销机器失败，appName: {}, environment: {}, groupName: {}, instanceId: {}", 
                     appName, environment, groupName, instanceId, e);
            return ApiResult.error("注销机器失败: " + e.getMessage());
        }
    }
} 