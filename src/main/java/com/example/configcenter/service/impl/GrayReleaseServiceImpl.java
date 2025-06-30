package com.example.configcenter.service.impl;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.GrayReleaseDto;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.GrayReleaseDetail;
import com.example.configcenter.entity.GrayReleasePlan;
import com.example.configcenter.enums.GrayReleaseEnum;
import com.example.configcenter.exception.BusinessException;
import com.example.configcenter.mapper.ConfigItemMapper;
import com.example.configcenter.mapper.GrayReleaseDetailMapper;
import com.example.configcenter.mapper.GrayReleasePlanMapper;
import com.example.configcenter.service.GrayReleaseService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 灰度发布服务实现类
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
            plan.setConfigKeys(objectMapper.writeValueAsString(request.getConfigKeys()));
            plan.setGrayStrategy(request.getGrayStrategy());
            plan.setGrayRules(objectMapper.writeValueAsString(request.getGrayRules()));
            plan.setRolloutPercentage(request.getRolloutPercentage());
            plan.setStatus(GrayReleaseEnum.Status.DRAFT.getCode());
            plan.setAutoComplete(request.getAutoComplete());
            plan.setAutoRollback(request.getAutoRollback());
            if (request.getRollbackCondition() != null) {
                plan.setRollbackCondition(objectMapper.writeValueAsString(request.getRollbackCondition()));
            }
            plan.setDescription(request.getDescription());
            plan.setCreator("admin"); // TODO: 从Security Context获取当前用户
            plan.setCreateBy("admin");
            plan.setUpdateBy("admin");
            
            grayReleasePlanMapper.insert(plan);
            
            // 创建灰度发布详情
            List<GrayReleaseDetail> details = new ArrayList<>();
            for (String configKey : request.getConfigKeys()) {
                // 查询当前配置项
                ConfigItem configItem = configItemMapper.findByKey(
                    request.getAppName(), request.getEnvironment(), request.getGroupName(), configKey);
                
                if (configItem != null) {
                    GrayReleaseDetail detail = new GrayReleaseDetail();
                    detail.setPlanId(plan.getId());
                    detail.setConfigId(configItem.getId());
                    detail.setConfigKey(configKey);
                    detail.setOldValue(configItem.getConfigValue());
                    detail.setNewValue(configItem.getConfigValue()); // 初始时新值等于旧值
                    detail.setStatus(GrayReleaseEnum.DetailStatus.PENDING.getCode());
                    detail.setCreateBy("admin");
                    detail.setUpdateBy("admin");
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
            throw new BusinessException("创建灰度发布计划失败: " + e.getMessage());
        }
    }
    
    @Override
    public PageResult<GrayReleaseDto.PlanResponse> queryPlans(GrayReleaseDto.PlanQueryRequest request) {
        try {
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            
            // 查询计划列表
            List<GrayReleasePlan> plans = grayReleasePlanMapper.selectByPage(request, offset, request.getPageSize());
            int total = grayReleasePlanMapper.countByQuery(request);
            
            // 转换为响应对象
            List<GrayReleaseDto.PlanResponse> responses = plans.stream()
                .map(this::convertToPlanResponse)
                .collect(Collectors.toList());
            
            return new PageResult<>(responses, total, request.getPageNum(), request.getPageSize());
            
        } catch (Exception e) {
            log.error("查询灰度发布计划失败", e);
            throw new BusinessException("查询灰度发布计划失败: " + e.getMessage());
        }
    }
    
    @Override
    public GrayReleaseDto.PlanResponse getPlanById(Long planId) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            GrayReleaseDto.PlanResponse response = convertToPlanResponse(plan);
            
            // 查询详情统计信息
            List<GrayReleaseDetail> details = grayReleaseDetailMapper.selectByPlanId(planId);
            response.setTotalConfigs(details.size());
            response.setGrayConfigs((int) details.stream().filter(d -> "GRAY".equals(d.getStatus())).count());
            response.setPublishedConfigs((int) details.stream().filter(d -> "PUBLISHED".equals(d.getStatus())).count());
            response.setRollbackConfigs((int) details.stream().filter(d -> "ROLLBACK".equals(d.getStatus())).count());
            
            return response;
            
        } catch (Exception e) {
            log.error("查询灰度发布计划详情失败，planId: {}", planId, e);
            throw new BusinessException("查询灰度发布计划详情失败: " + e.getMessage());
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
            log.error("查询灰度发布配置详情失败，planId: {}", planId, e);
            throw new BusinessException("查询灰度发布配置详情失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void startGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            if (!GrayReleaseEnum.Status.DRAFT.getCode().equals(plan.getStatus())) {
                throw new BusinessException("只有草稿状态的计划才能开始发布");
            }
            
            // 更新计划状态
            plan.setStatus(GrayReleaseEnum.Status.ACTIVE.getCode());
            plan.setStartTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 更新详情状态
            grayReleaseDetailMapper.updateStatusByPlanId(planId, 
                GrayReleaseEnum.DetailStatus.GRAY.getCode(), operator);
            
            log.info("开始灰度发布成功，计划ID: {}, 操作者: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("开始灰度发布失败，planId: {}", planId, e);
            throw new BusinessException("开始灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void pauseGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            if (!GrayReleaseEnum.Status.ACTIVE.getCode().equals(plan.getStatus())) {
                throw new BusinessException("只有进行中的计划才能暂停");
            }
            
            // 更新计划状态
            grayReleasePlanMapper.updateStatus(planId, GrayReleaseEnum.Status.PAUSED.getCode(), operator);
            
            log.info("暂停灰度发布成功，计划ID: {}, 操作者: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("暂停灰度发布失败，planId: {}", planId, e);
            throw new BusinessException("暂停灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void resumeGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            if (!GrayReleaseEnum.Status.PAUSED.getCode().equals(plan.getStatus())) {
                throw new BusinessException("只有暂停状态的计划才能恢复");
            }
            
            // 更新计划状态
            grayReleasePlanMapper.updateStatus(planId, GrayReleaseEnum.Status.ACTIVE.getCode(), operator);
            
            log.info("恢复灰度发布成功，计划ID: {}, 操作者: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("恢复灰度发布失败，planId: {}", planId, e);
            throw new BusinessException("恢复灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void completeGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            if (!Arrays.asList(GrayReleaseEnum.Status.ACTIVE.getCode(), GrayReleaseEnum.Status.PAUSED.getCode())
                    .contains(plan.getStatus())) {
                throw new BusinessException("只有进行中或暂停状态的计划才能完成");
            }
            
            // 更新计划状态
            plan.setStatus(GrayReleaseEnum.Status.COMPLETED.getCode());
            plan.setEndTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 更新详情状态
            grayReleaseDetailMapper.updateStatusByPlanId(planId, 
                GrayReleaseEnum.DetailStatus.PUBLISHED.getCode(), operator);
            
            log.info("完成灰度发布成功，计划ID: {}, 操作者: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("完成灰度发布失败，planId: {}", planId, e);
            throw new BusinessException("完成灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void rollbackGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            if (!Arrays.asList(GrayReleaseEnum.Status.ACTIVE.getCode(), GrayReleaseEnum.Status.PAUSED.getCode())
                    .contains(plan.getStatus())) {
                throw new BusinessException("只有进行中或暂停状态的计划才能回滚");
            }
            
            // 更新计划状态
            plan.setStatus(GrayReleaseEnum.Status.ROLLBACK.getCode());
            plan.setEndTime(System.currentTimeMillis());
            plan.setUpdateBy(operator);
            grayReleasePlanMapper.updateById(plan);
            
            // 更新详情状态
            grayReleaseDetailMapper.updateStatusByPlanId(planId, 
                GrayReleaseEnum.DetailStatus.ROLLBACK.getCode(), operator);
            
            log.info("回滚灰度发布成功，计划ID: {}, 操作者: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("回滚灰度发布失败，planId: {}", planId, e);
            throw new BusinessException("回滚灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public void cancelGrayRelease(Long planId, String operator) {
        try {
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new BusinessException("灰度发布计划不存在");
            }
            
            if (GrayReleaseEnum.Status.COMPLETED.getCode().equals(plan.getStatus())) {
                throw new BusinessException("已完成的计划不能取消");
            }
            
            // 删除计划和详情
            grayReleasePlanMapper.deleteById(planId);
            
            log.info("取消灰度发布成功，计划ID: {}, 操作者: {}", planId, operator);
            
        } catch (Exception e) {
            log.error("取消灰度发布失败，planId: {}", planId, e);
            throw new BusinessException("取消灰度发布失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean isGrayHit(String appName, String environment, String configKey, 
                            String instanceId, String instanceIp, String userId) {
        try {
            // 查询进行中的灰度发布计划
            List<GrayReleasePlan> activePlans = grayReleasePlanMapper.selectActiveByConfig(appName, environment, configKey);
            
            for (GrayReleasePlan plan : activePlans) {
                if (checkGrayHit(plan, instanceId, instanceIp, userId)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("判断灰度命中失败", e);
            return false;
        }
    }
    
    @Override
    public String getConfigValue(String appName, String environment, String groupName, 
                                String configKey, String instanceId, String instanceIp, String userId) {
        try {
            // 先查询是否命中灰度
            if (isGrayHit(appName, environment, configKey, instanceId, instanceIp, userId)) {
                // 查询灰度配置值
                GrayReleaseDetail detail = grayReleaseDetailMapper.selectActiveByConfig(appName, environment, configKey);
                if (detail != null && StringUtils.hasText(detail.getGrayValue())) {
                    log.debug("返回灰度配置值，configKey: {}, grayValue: {}", configKey, detail.getGrayValue());
                    return detail.getGrayValue();
                }
            }
            
            // 返回原配置值
            ConfigItem configItem = configItemMapper.findByKey(appName, environment, groupName, configKey);
            if (configItem != null) {
                log.debug("返回原配置值，configKey: {}, value: {}", configKey, configItem.getConfigValue());
                return configItem.getConfigValue();
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("获取配置值失败", e);
            return null;
        }
    }
    
    @Override
    public void autoProcessGrayRelease() {
        // TODO: 实现自动处理逻辑，如自动回滚、自动完成等
        log.info("自动处理灰度发布任务执行");
    }
    
    /**
     * 检查是否命中灰度
     */
    private boolean checkGrayHit(GrayReleasePlan plan, String instanceId, String instanceIp, String userId) {
        try {
            String grayRulesJson = plan.getGrayRules();
            if (!StringUtils.hasText(grayRulesJson)) {
                return false;
            }
            
            Map<String, Object> grayRules = objectMapper.readValue(grayRulesJson, 
                new TypeReference<Map<String, Object>>() {});
            
            String strategy = plan.getGrayStrategy();
            
            switch (strategy) {
                case "IP_WHITELIST":
                    return checkIpWhitelist(grayRules, instanceIp);
                case "USER_WHITELIST":
                    return checkUserWhitelist(grayRules, userId);
                case "PERCENTAGE":
                    return checkPercentage(grayRules, instanceId, plan.getRolloutPercentage());
                case "CANARY":
                    return checkCanary(grayRules, instanceIp);
                default:
                    return false;
            }
            
        } catch (Exception e) {
            log.error("检查灰度命中失败", e);
            return false;
        }
    }
    
    /**
     * 检查IP白名单
     */
    @SuppressWarnings("unchecked")
    private boolean checkIpWhitelist(Map<String, Object> grayRules, String instanceIp) {
        if (!StringUtils.hasText(instanceIp)) {
            return false;
        }
        
        List<String> ipWhitelist = (List<String>) grayRules.get("ipWhitelist");
        return ipWhitelist != null && ipWhitelist.contains(instanceIp);
    }
    
    /**
     * 检查用户白名单
     */
    @SuppressWarnings("unchecked")
    private boolean checkUserWhitelist(Map<String, Object> grayRules, String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        
        List<String> userWhitelist = (List<String>) grayRules.get("userWhitelist");
        return userWhitelist != null && userWhitelist.contains(userId);
    }
    
    /**
     * 检查按比例发布
     */
    private boolean checkPercentage(Map<String, Object> grayRules, String instanceId, Integer rolloutPercentage) {
        if (!StringUtils.hasText(instanceId) || rolloutPercentage == null || rolloutPercentage <= 0) {
            return false;
        }
        
        // 使用一致性哈希确保相同实例的结果稳定
        int hash = Math.abs(instanceId.hashCode());
        return (hash % 100) < rolloutPercentage;
    }
    
    /**
     * 检查金丝雀发布
     */
    @SuppressWarnings("unchecked")
    private boolean checkCanary(Map<String, Object> grayRules, String instanceIp) {
        if (!StringUtils.hasText(instanceIp)) {
            return false;
        }
        
        Map<String, Object> canaryRules = (Map<String, Object>) grayRules.get("canary");
        if (canaryRules == null) {
            return false;
        }
        
        List<String> canaryInstances = (List<String>) canaryRules.get("canaryInstances");
        return canaryInstances != null && canaryInstances.contains(instanceIp);
    }
    
    /**
     * 转换为计划响应对象
     */
    private GrayReleaseDto.PlanResponse convertToPlanResponse(GrayReleasePlan plan) {
        try {
            GrayReleaseDto.PlanResponse response = new GrayReleaseDto.PlanResponse();
            BeanUtils.copyProperties(plan, response);
            
            // 解析JSON字段
            if (StringUtils.hasText(plan.getConfigKeys())) {
                response.setConfigKeys(objectMapper.readValue(plan.getConfigKeys(), 
                    new TypeReference<List<String>>() {}));
            }
            
            if (StringUtils.hasText(plan.getGrayRules())) {
                response.setGrayRules(objectMapper.readValue(plan.getGrayRules(), 
                    new TypeReference<Map<String, Object>>() {}));
            }
            
            if (StringUtils.hasText(plan.getRollbackCondition())) {
                response.setRollbackCondition(objectMapper.readValue(plan.getRollbackCondition(), 
                    new TypeReference<Map<String, Object>>() {}));
            }
            
            return response;
            
        } catch (Exception e) {
            log.error("转换计划响应对象失败", e);
            throw new BusinessException("数据转换失败");
        }
    }
    
    /**
     * 转换为详情响应对象
     */
    private GrayReleaseDto.DetailResponse convertToDetailResponse(GrayReleaseDetail detail) {
        GrayReleaseDto.DetailResponse response = new GrayReleaseDto.DetailResponse();
        BeanUtils.copyProperties(detail, response);
        return response;
    }
} 