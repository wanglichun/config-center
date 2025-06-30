package com.example.configcenter.service.impl;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.GrayReleaseDto;
import com.example.configcenter.entity.GrayReleasePlan;
import com.example.configcenter.entity.GrayReleaseDetail;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.enums.GrayReleaseEnum;
import com.example.configcenter.mapper.GrayReleasePlanMapper;
import com.example.configcenter.mapper.GrayReleaseDetailMapper;
import com.example.configcenter.mapper.ConfigItemMapper;
import com.example.configcenter.service.GrayReleaseService;
import com.example.configcenter.service.ConfigPushService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 灰度发布服务实现
 */
@Service
public class GrayReleaseServiceImpl implements GrayReleaseService {
    
    private static final Logger log = LoggerFactory.getLogger(GrayReleaseServiceImpl.class);
    
    @Autowired
    private GrayReleasePlanMapper grayReleasePlanMapper;
    
    @Autowired
    private GrayReleaseDetailMapper grayReleaseDetailMapper;
    
    @Autowired
    private ConfigItemMapper configItemMapper;
    
    @Autowired
    private ConfigPushService configPushService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    @Transactional
    public Long createPlan(GrayReleaseDto.CreatePlanRequest request) {
        try {
            // 创建灰度发布计划
            GrayReleasePlan plan = new GrayReleasePlan();
            plan.setPlanName(request.getPlanName());
            plan.setAppName(request.getAppName());
            plan.setEnvironment(request.getEnvironment());
            plan.setGroupName(request.getGroupName());
            plan.setConfigKeys(String.join(",", request.getConfigKeys()));
            plan.setGrayStrategy(request.getGrayStrategy());
            plan.setGrayRules(objectMapper.writeValueAsString(request.getGrayRules()));
            plan.setRolloutPercentage(request.getRolloutPercentage());
            plan.setStatus("DRAFT");
            plan.setAutoComplete(request.getAutoComplete());
            plan.setAutoRollback(request.getAutoRollback());
            plan.setDescription(request.getDescription());
            plan.setCreator("admin"); // TODO: 从当前用户获取
            plan.setCreateBy("admin");
            
            if (request.getRollbackCondition() != null) {
                plan.setRollbackCondition(objectMapper.writeValueAsString(request.getRollbackCondition()));
            }
            
            grayReleasePlanMapper.insert(plan);
            
            // 创建灰度发布详情
            List<GrayReleaseDetail> details = new ArrayList<>();
            for (String configKey : request.getConfigKeys()) {
                // 查询配置项
                ConfigItem configItem = configItemMapper.findByKey(
                    request.getAppName(), request.getEnvironment(), request.getGroupName(), configKey);
                
                if (configItem != null) {
                    GrayReleaseDetail detail = new GrayReleaseDetail();
                    detail.setPlanId(plan.getId());
                    detail.setConfigId(configItem.getId());
                    detail.setConfigKey(configKey);
                    detail.setOldValue(configItem.getConfigValue());
                    detail.setNewValue(configItem.getConfigValue()); // 初始值相同
                    detail.setStatus("PENDING");
                    detail.setCreateBy("admin");
                    details.add(detail);
                }
            }
            
            if (!details.isEmpty()) {
                grayReleaseDetailMapper.batchInsert(details);
            }
            
            log.info("创建灰度发布计划成功，计划ID: {}, 计划名称: {}", plan.getId(), plan.getPlanName());
            return plan.getId();
            
        } catch (Exception e) {
            log.error("创建灰度发布计划失败", e);
            throw new RuntimeException("创建灰度发布计划失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResult<GrayReleaseDto.PlanResponse> queryPlans(GrayReleaseDto.PlanQueryRequest request) {
        try {
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            int limit = request.getPageSize();
            
            List<GrayReleasePlan> plans = grayReleasePlanMapper.selectByPage(request, offset, limit);
            int total = grayReleasePlanMapper.countByQuery(request);
            
            List<GrayReleaseDto.PlanResponse> responses = plans.stream()
                .map(this::convertToPlanResponse)
                .collect(Collectors.toList());
            
            return new PageResult<>(responses, total, request.getPageNum(), request.getPageSize());
            
        } catch (Exception e) {
            log.error("查询灰度发布计划失败", e);
            throw new RuntimeException("查询灰度发布计划失败: " + e.getMessage());
        }
    }
    
    @Override
    public GrayReleaseDto.PlanResponse getPlanById(Long planId) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            return convertToPlanResponse(plan);
            
        } catch (Exception e) {
            log.error("查询灰度发布计划详情失败，planId: {}", planId, e);
            throw new RuntimeException("查询灰度发布计划详情失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<GrayReleaseDto.DetailResponse> getPlanDetails(Long planId) {
        try {
            List<GrayReleaseDetail> details = grayReleaseDetailMapper.selectByPlanId(planId);
            return details.stream()
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
                
        } catch (Exception e) {
            log.error("查询灰度发布详情失败，planId: {}", planId, e);
            throw new RuntimeException("查询灰度发布详情失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void startGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            
            if (!"DRAFT".equals(plan.getStatus())) {
                throw new RuntimeException("只有草稿状态的计划才能开始执行");
            }
            
            // 更新计划状态
            plan.setStatus("EXECUTING");
            plan.setStartTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 更新详情状态
            grayReleaseDetailMapper.updateStatusByPlanId(planId, "PENDING", operator);
            
            // 推送灰度配置
            configPushService.pushGrayConfig(planId);
            
            log.info("开始灰度发布成功，计划ID: {}, 操作人: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("开始灰度发布失败，planId: {}", planId, e);
            throw new RuntimeException("开始灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void pauseGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            
            if (!"EXECUTING".equals(plan.getStatus())) {
                throw new RuntimeException("只有执行中的计划才能暂停");
            }
            
            plan.setStatus("PAUSED");
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            log.info("暂停灰度发布成功，计划ID: {}, 操作人: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("暂停灰度发布失败，planId: {}", planId, e);
            throw new RuntimeException("暂停灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void resumeGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            
            if (!"PAUSED".equals(plan.getStatus())) {
                throw new RuntimeException("只有暂停状态的计划才能恢复");
            }
            
            plan.setStatus("EXECUTING");
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            log.info("恢复灰度发布成功，计划ID: {}, 操作人: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("恢复灰度发布失败，planId: {}", planId, e);
            throw new RuntimeException("恢复灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void completeGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            
            if (!"EXECUTING".equals(plan.getStatus())) {
                throw new RuntimeException("只有执行中的计划才能完成");
            }
            
            plan.setStatus("COMPLETED");
            plan.setEndTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 更新详情状态为已发布
            grayReleaseDetailMapper.updateStatusByPlanId(planId, "SUCCESS", operator);
            
            log.info("完成灰度发布成功，计划ID: {}, 操作人: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("完成灰度发布失败，planId: {}", planId, e);
            throw new RuntimeException("完成灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void rollbackGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            
            String currentStatus = plan.getStatus();
            if (!Arrays.asList("EXECUTING", "COMPLETED").contains(currentStatus)) {
                throw new RuntimeException("只有执行中或已完成的计划才能回滚");
            }
            
            plan.setStatus("ROLLBACK");
            plan.setEndTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 回滚配置
            configPushService.rollbackGrayConfig(planId);
            
            log.info("回滚灰度发布成功，计划ID: {}, 操作人: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("回滚灰度发布失败，planId: {}", planId, e);
            throw new RuntimeException("回滚灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void cancelGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在");
            }
            
            if ("COMPLETED".equals(plan.getStatus())) {
                throw new RuntimeException("已完成的计划不能取消");
            }
            
            plan.setStatus("CANCELLED");
            plan.setEndTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            log.info("取消灰度发布成功，计划ID: {}, 操作人: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("取消灰度发布失败，planId: {}", planId, e);
            throw new RuntimeException("取消灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isGrayHit(String appName, String environment, String configKey, 
                            String instanceId, String instanceIp, String userId) {
        try {
            // 查询活跃的灰度发布计划
            List<GrayReleasePlan> activePlans = grayReleasePlanMapper.selectActiveByConfig(
                appName, environment, configKey);
            
            if (activePlans.isEmpty()) {
                return false;
            }
            
            // 遍历活跃计划，检查是否命中灰度规则
            for (GrayReleasePlan plan : activePlans) {
                if (checkGrayRules(plan, instanceId, instanceIp, userId)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("检查灰度命中失败", e);
            return false;
        }
    }
    
    @Override
    public String getConfigValue(String appName, String environment, String groupName, 
                                String configKey, String instanceId, String instanceIp, String userId) {
        try {
            // 检查是否命中灰度
            if (isGrayHit(appName, environment, configKey, instanceId, instanceIp, userId)) {
                // 获取灰度配置值
                GrayReleaseDetail detail = grayReleaseDetailMapper.selectActiveByConfig(
                    appName, environment, configKey);
                if (detail != null && detail.getGrayValue() != null) {
                    return detail.getGrayValue();
                }
            }
            
            // 返回正常配置值
            ConfigItem configItem = configItemMapper.findByKey(
                appName, environment, groupName, configKey);
            return configItem != null ? configItem.getConfigValue() : null;
            
        } catch (Exception e) {
            log.error("获取配置值失败", e);
            return null;
        }
    }
    
    @Override
    public void autoProcessGrayRelease() {
        // TODO: 实现自动处理灰度发布逻辑
        log.info("自动处理灰度发布任务执行");
    }
    
    /**
     * 检查灰度规则
     */
    private boolean checkGrayRules(GrayReleasePlan plan, String instanceId, String instanceIp, String userId) {
        try {
            String strategy = plan.getGrayStrategy();
            String rulesJson = plan.getGrayRules();
            
            if (rulesJson == null) {
                return false;
            }
            
            @SuppressWarnings("unchecked")
            Map<String, Object> rules = objectMapper.readValue(rulesJson, Map.class);
            
            switch (strategy) {
                case "IP_WHITELIST":
                    @SuppressWarnings("unchecked")
                    List<String> ipWhitelist = (List<String>) rules.get("ipWhitelist");
                    return ipWhitelist != null && ipWhitelist.contains(instanceIp);
                    
                case "PERCENTAGE":
                    Integer percentage = (Integer) rules.get("percentage");
                    if (percentage == null || percentage <= 0) {
                        return false;
                    }
                    // 基于实例ID的哈希值来决定是否命中
                    int hash = Math.abs((instanceId != null ? instanceId : instanceIp).hashCode());
                    return (hash % 100) < percentage;
                    
                case "MANUAL":
                    @SuppressWarnings("unchecked")
                    List<String> instances = (List<String>) rules.get("instances");
                    return instances != null && instances.contains(instanceId);
                    
                default:
                    return false;
            }
            
        } catch (Exception e) {
            log.error("检查灰度规则失败", e);
            return false;
        }
    }
    
    /**
     * 转换为计划响应对象
     */
    private GrayReleaseDto.PlanResponse convertToPlanResponse(GrayReleasePlan plan) {
        GrayReleaseDto.PlanResponse response = new GrayReleaseDto.PlanResponse();
        response.setId(plan.getId());
        response.setPlanName(plan.getPlanName());
        response.setAppName(plan.getAppName());
        response.setEnvironment(plan.getEnvironment());
        response.setGroupName(plan.getGroupName());
        
        if (plan.getConfigKeys() != null) {
            response.setConfigKeys(Arrays.asList(plan.getConfigKeys().split(",")));
        }
        
        response.setGrayStrategy(plan.getGrayStrategy());
        response.setRolloutPercentage(plan.getRolloutPercentage());
        response.setStatus(plan.getStatus());
        response.setStartTime(plan.getStartTime());
        response.setEndTime(plan.getEndTime());
        response.setAutoComplete(plan.getAutoComplete());
        response.setAutoRollback(plan.getAutoRollback());
        response.setDescription(plan.getDescription());
        response.setCreator(plan.getCreator());
        response.setApprover(plan.getApprover());
        response.setApprovalTime(plan.getApprovalTime());
        
        // 处理时间转换
        if (plan.getCreateTime() != null) {
            response.setCreateTime(plan.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (plan.getUpdateTime() != null) {
            response.setUpdateTime(plan.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        
        return response;
    }
    
    /**
     * 转换为详情响应对象
     */
    private GrayReleaseDto.DetailResponse convertToDetailResponse(GrayReleaseDetail detail) {
        GrayReleaseDto.DetailResponse response = new GrayReleaseDto.DetailResponse();
        response.setId(detail.getId());
        response.setPlanId(detail.getPlanId());
        response.setConfigId(detail.getConfigId());
        response.setConfigKey(detail.getConfigKey());
        response.setOldValue(detail.getOldValue());
        response.setNewValue(detail.getNewValue());
        response.setGrayValue(detail.getGrayValue());
        response.setStatus(detail.getStatus());
        response.setGrayStartTime(detail.getGrayStartTime());
        response.setPublishTime(detail.getPublishTime());
        response.setRollbackTime(detail.getRollbackTime());
        
        return response;
    }
} 