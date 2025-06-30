package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.GrayReleaseDto;
import com.example.configcenter.service.GrayReleaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 灰度发布控制器
 */
@RestController
@RequestMapping("/api/gray-release")
public class GrayReleaseController {
    
    private static final Logger log = LoggerFactory.getLogger(GrayReleaseController.class);
    
    @Autowired
    private GrayReleaseService grayReleaseService;
    
    /**
     * 创建灰度发布计划
     */
    @PostMapping("/plan")
    public ApiResult<Long> createPlan(@Validated @RequestBody GrayReleaseDto.CreatePlanRequest request,
                                      Authentication authentication) {
        try {
            Long planId = grayReleaseService.createPlan(request);
            log.info("用户 {} 创建灰度发布计划成功，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success(planId);
        } catch (Exception e) {
            log.error("创建灰度发布计划失败", e);
            return ApiResult.error("创建灰度发布计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询灰度发布计划
     */
    @GetMapping("/plan/page")
    public ApiResult<PageResult<GrayReleaseDto.PlanResponse>> queryPlans(GrayReleaseDto.PlanQueryRequest request) {
        try {
            PageResult<GrayReleaseDto.PlanResponse> result = grayReleaseService.queryPlans(request);
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("查询灰度发布计划失败", e);
            return ApiResult.error("查询灰度发布计划失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询灰度发布计划详情
     */
    @GetMapping("/plan/{planId}")
    public ApiResult<GrayReleaseDto.PlanResponse> getPlanById(@PathVariable Long planId) {
        try {
            GrayReleaseDto.PlanResponse plan = grayReleaseService.getPlanById(planId);
            return ApiResult.success(plan);
        } catch (Exception e) {
            log.error("查询灰度发布计划详情失败，planId: {}", planId, e);
            return ApiResult.error("查询灰度发布计划详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询灰度发布配置详情
     */
    @GetMapping("/plan/{planId}/details")
    public ApiResult<List<GrayReleaseDto.DetailResponse>> getPlanDetails(@PathVariable Long planId) {
        try {
            List<GrayReleaseDto.DetailResponse> details = grayReleaseService.getPlanDetails(planId);
            return ApiResult.success(details);
        } catch (Exception e) {
            log.error("查询灰度发布配置详情失败，planId: {}", planId, e);
            return ApiResult.error("查询灰度发布配置详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 开始灰度发布
     */
    @PostMapping("/plan/{planId}/start")
    public ApiResult<Void> startGrayRelease(@PathVariable Long planId,
                                           Authentication authentication) {
        try {
            grayReleaseService.startGrayRelease(planId, authentication.getName());
            log.info("用户 {} 开始灰度发布，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("开始灰度发布失败，planId: {}", planId, e);
            return ApiResult.error("开始灰度发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 暂停灰度发布
     */
    @PostMapping("/plan/{planId}/pause")
    public ApiResult<Void> pauseGrayRelease(@PathVariable Long planId,
                                           Authentication authentication) {
        try {
            grayReleaseService.pauseGrayRelease(planId, authentication.getName());
            log.info("用户 {} 暂停灰度发布，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("暂停灰度发布失败，planId: {}", planId, e);
            return ApiResult.error("暂停灰度发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 恢复灰度发布
     */
    @PostMapping("/plan/{planId}/resume")
    public ApiResult<Void> resumeGrayRelease(@PathVariable Long planId,
                                            Authentication authentication) {
        try {
            grayReleaseService.resumeGrayRelease(planId, authentication.getName());
            log.info("用户 {} 恢复灰度发布，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("恢复灰度发布失败，planId: {}", planId, e);
            return ApiResult.error("恢复灰度发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 完成灰度发布(全量发布)
     */
    @PostMapping("/plan/{planId}/complete")
    public ApiResult<Void> completeGrayRelease(@PathVariable Long planId,
                                              Authentication authentication) {
        try {
            grayReleaseService.completeGrayRelease(planId, authentication.getName());
            log.info("用户 {} 完成灰度发布，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("完成灰度发布失败，planId: {}", planId, e);
            return ApiResult.error("完成灰度发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 回滚灰度发布
     */
    @PostMapping("/plan/{planId}/rollback")
    public ApiResult<Void> rollbackGrayRelease(@PathVariable Long planId,
                                              Authentication authentication) {
        try {
            grayReleaseService.rollbackGrayRelease(planId, authentication.getName());
            log.info("用户 {} 回滚灰度发布，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("回滚灰度发布失败，planId: {}", planId, e);
            return ApiResult.error("回滚灰度发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 取消灰度发布
     */
    @PostMapping("/plan/{planId}/cancel")
    public ApiResult<Void> cancelGrayRelease(@PathVariable Long planId,
                                            Authentication authentication) {
        try {
            grayReleaseService.cancelGrayRelease(planId, authentication.getName());
            log.info("用户 {} 取消灰度发布，计划ID: {}", authentication.getName(), planId);
            return ApiResult.success();
        } catch (Exception e) {
            log.error("取消灰度发布失败，planId: {}", planId, e);
            return ApiResult.error("取消灰度发布失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取配置值(支持灰度发布)
     */
    @GetMapping("/config/value")
    public ApiResult<String> getConfigValue(@RequestParam String appName,
                                           @RequestParam String environment,
                                           @RequestParam String groupName,
                                           @RequestParam String configKey,
                                           @RequestParam(required = false) String instanceId,
                                           @RequestParam(required = false) String userId,
                                           HttpServletRequest request) {
        try {
            String instanceIp = getClientIp(request);
            String configValue = grayReleaseService.getConfigValue(
                appName, environment, groupName, configKey, instanceId, instanceIp, userId);
            return ApiResult.success(configValue);
        } catch (Exception e) {
            log.error("获取配置值失败，appName: {}, environment: {}, configKey: {}", 
                     appName, environment, configKey, e);
            return ApiResult.error("获取配置值失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取实例列表
     */
    @GetMapping("/instances")
    public ApiResult<List<String>> getInstances(@RequestParam String appName,
                                               @RequestParam String environment,
                                               @RequestParam(required = false) String groupName,
                                               @RequestParam(required = false) String configKey) {
        try {
            List<String> instances = grayReleaseService.getInstances(appName, environment, groupName, configKey);
            return ApiResult.success(instances);
        } catch (Exception e) {
            log.error("获取实例列表失败，appName: {}, environment: {}", appName, environment, e);
            return ApiResult.error("获取实例列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
} 