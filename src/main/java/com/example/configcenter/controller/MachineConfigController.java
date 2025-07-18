package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.service.MachineConfigSubscriptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 机器配置订阅控制器
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/machine-config")
@CrossOrigin(origins = "*")
public class MachineConfigController {

    @Autowired
    private MachineConfigSubscriptionService machineConfigSubscriptionService;

    /**
     * 注册机器实例
     */
    @PostMapping("/register")
    public ApiResult<Boolean> registerMachine(@RequestParam String appName,
                                             @RequestParam String environment,
                                             @RequestParam String groupName,
                                             @RequestParam String instanceId,
                                             @RequestParam String instanceIp,
                                             @RequestParam List<String> configKeys) {
        try {
            boolean result = machineConfigSubscriptionService.registerMachine(
                appName, environment, groupName, instanceId, instanceIp, configKeys);
            return result ? ApiResult.success(true) : ApiResult.error("注册机器实例失败");
        } catch (Exception e) {
            log.error("注册机器实例失败", e);
            return ApiResult.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 注销机器实例
     */
    @PostMapping("/unregister")
    public ApiResult<Boolean> unregisterMachine(@RequestParam String appName,
                                               @RequestParam String environment,
                                               @RequestParam String groupName,
                                               @RequestParam String instanceId) {
        try {
            boolean result = machineConfigSubscriptionService.unregisterMachine(
                appName, environment, groupName, instanceId);
            return result ? ApiResult.success(true) : ApiResult.error("注销机器实例失败");
        } catch (Exception e) {
            log.error("注销机器实例失败", e);
            return ApiResult.error("注销失败：" + e.getMessage());
        }
    }

    /**
     * 获取订阅指定配置的机器列表
     */
    @GetMapping("/subscribers")
    public ApiResult<List<String>> getSubscribedMachines(@RequestParam String appName,
                                                         @RequestParam String environment,
                                                         @RequestParam String groupName,
                                                         @RequestParam String configKey) {
        try {
            List<String> machines = machineConfigSubscriptionService.getSubscribedMachines(
                appName, environment, groupName, configKey);
            return ApiResult.success(machines);
        } catch (Exception e) {
            log.error("获取订阅机器列表失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取机器实例的配置信息
     */
    @GetMapping("/configs/{instanceId}")
    public ApiResult<Map<String, String>> getMachineConfigs(@PathVariable String instanceId) {
        try {
            Map<String, String> configs = machineConfigSubscriptionService.getMachineConfigs(instanceId);
            return ApiResult.success(configs);
        } catch (Exception e) {
            log.error("获取机器配置失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 心跳检测
     */
    @PostMapping("/heartbeat/{instanceId}")
    public ApiResult<Boolean> heartbeat(@PathVariable String instanceId) {
        try {
            boolean alive = machineConfigSubscriptionService.heartbeat(instanceId);
            return ApiResult.success(alive);
        } catch (Exception e) {
            log.error("心跳检测失败", e);
            return ApiResult.error("心跳失败：" + e.getMessage());
        }
    }
} 