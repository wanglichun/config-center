package com.example.configcenter.service;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.GrayReleaseDto;
import com.example.configcenter.entity.GrayReleasePlan;

import java.util.List;

/**
 * 灰度发布服务接口
 */
public interface GrayReleaseService {
    
    /**
     * 创建灰度发布计划
     */
    Long createPlan(GrayReleaseDto.CreatePlanRequest request);
    
    /**
     * 分页查询灰度发布计划
     */
    PageResult<GrayReleaseDto.PlanResponse> queryPlans(GrayReleaseDto.PlanQueryRequest request);
    
    /**
     * 根据ID查询灰度发布计划
     */
    GrayReleaseDto.PlanResponse getPlanById(Long planId);
    
    /**
     * 查询灰度发布详情
     */
    List<GrayReleaseDto.DetailResponse> getPlanDetails(Long planId);
    
    /**
     * 开始灰度发布
     */
    void startGrayRelease(Long planId, String operator);
    
    /**
     * 暂停灰度发布
     */
    void pauseGrayRelease(Long planId, String operator);
    
    /**
     * 恢复灰度发布
     */
    void resumeGrayRelease(Long planId, String operator);
    
    /**
     * 完成灰度发布(全量发布)
     */
    void completeGrayRelease(Long planId, String operator);
    
    /**
     * 回滚灰度发布
     */
    void rollbackGrayRelease(Long planId, String operator);
    
    /**
     * 取消灰度发布
     */
    void cancelGrayRelease(Long planId, String operator);
    
    /**
     * 根据灰度规则判断是否命中灰度
     */
    boolean isGrayHit(String appName, String environment, String configKey, 
                      String instanceId, String instanceIp, String userId);
    
    /**
     * 获取配置值(考虑灰度发布)
     */
    String getConfigValue(String appName, String environment, String groupName, 
                         String configKey, String instanceId, String instanceIp, String userId);
    
    /**
     * 自动检查并处理灰度发布
     */
    void autoProcessGrayRelease();
    
    /**
     * 获取实例列表
     */
    List<String> getInstances(String appName, String environment, String groupName, String configKey);
} 