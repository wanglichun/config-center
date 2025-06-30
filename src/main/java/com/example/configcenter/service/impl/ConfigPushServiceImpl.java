package com.example.configcenter.service.impl;

import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.GrayReleaseDetail;
import com.example.configcenter.entity.GrayReleasePlan;
import com.example.configcenter.mapper.ConfigItemMapper;
import com.example.configcenter.mapper.GrayReleaseDetailMapper;
import com.example.configcenter.mapper.GrayReleasePlanMapper;
import com.example.configcenter.service.ConfigPushService;
import com.example.configcenter.service.ZooKeeperService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配置推送服务实现类
 */
@Service
public class ConfigPushServiceImpl implements ConfigPushService {
    
    private static final Logger log = LoggerFactory.getLogger(ConfigPushServiceImpl.class);
    
    @Autowired
    private ZooKeeperService zooKeeperService;
    
    @Autowired
    private ConfigItemMapper configItemMapper;
    
    @Autowired
    private GrayReleasePlanMapper grayReleasePlanMapper;
    
    @Autowired
    private GrayReleaseDetailMapper grayReleaseDetailMapper;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 推送配置到ZooKeeper
     */
    @Override
    public void pushConfigToZooKeeper(String appName, String environment, String groupName, String configKey, String configValue) {
        try {
            String zkPath = buildConfigPath(appName, environment, groupName, configKey);
            
            log.info("开始推送配置到ZooKeeper: path={}, value={}", zkPath, configValue);
            
            boolean success = zooKeeperService.publishConfig(zkPath, configValue);
            
            if (success) {
                log.info("配置推送成功: {}", zkPath);
                // 通知相关实例刷新配置
                notifyConfigChange(appName, environment, configKey);
            } else {
                log.error("配置推送失败: {}", zkPath);
                throw new RuntimeException("配置推送到ZooKeeper失败");
            }
            
        } catch (Exception e) {
            log.error("推送配置到ZooKeeper异常: appName={}, environment={}, configKey={}", 
                     appName, environment, configKey, e);
            throw new RuntimeException("推送配置失败: " + e.getMessage());
        }
    }

    /**
     * 批量推送配置
     */
    @Override
    public void batchPushConfig(String appName, String environment, List<ConfigPushItem> configItems) {
        if (CollectionUtils.isEmpty(configItems)) {
            log.warn("批量推送配置项为空");
            return;
        }
        
        try {
            log.info("开始批量推送配置: appName={}, environment={}, count={}", 
                    appName, environment, configItems.size());
            
            int successCount = 0;
            List<String> failedKeys = new ArrayList<>();
            
            for (ConfigPushItem item : configItems) {
                try {
                    // 获取配置项详细信息
                    ConfigItem configItem = configItemMapper.findByKeyWithoutGroup(appName, environment, item.getConfigKey());
                    if (configItem == null) {
                        log.warn("配置项不存在: {}", item.getConfigKey());
                        failedKeys.add(item.getConfigKey());
                        continue;
                    }
                    
                    pushConfigToZooKeeper(appName, environment, configItem.getGroupName(), 
                                        item.getConfigKey(), item.getConfigValue());
                    successCount++;
                    
                } catch (Exception e) {
                    log.error("推送配置失败: key={}", item.getConfigKey(), e);
                    failedKeys.add(item.getConfigKey());
                }
            }
            
            log.info("批量推送配置完成: 总数={}, 成功={}, 失败={}", 
                    configItems.size(), successCount, failedKeys.size());
            
            if (!failedKeys.isEmpty()) {
                log.warn("推送失败的配置键: {}", String.join(", ", failedKeys));
            }
            
            // 批量通知实例刷新
            List<String> configKeys = configItems.stream()
                    .map(ConfigPushItem::getConfigKey)
                    .collect(Collectors.toList());
            notifyBatchConfigChange(appName, environment, configKeys);
            
        } catch (Exception e) {
            log.error("批量推送配置异常: appName={}, environment={}", appName, environment, e);
            throw new RuntimeException("批量推送配置失败: " + e.getMessage());
        }
    }

    /**
     * 推送灰度配置
     */
    @Override
    public void pushGrayConfig(Long planId) {
        try {
            log.info("开始推送灰度配置: planId={}", planId);
            
            // 获取灰度发布计划
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在: " + planId);
            }
            
            // 获取灰度发布详情
            List<GrayReleaseDetail> details = grayReleaseDetailMapper.selectByPlanId(planId);
            if (CollectionUtils.isEmpty(details)) {
                throw new RuntimeException("灰度发布详情为空: " + planId);
            }
            
            // 根据不同策略推送灰度配置
            String strategy = plan.getGrayStrategy();
            switch (strategy) {
                case "IP_WHITELIST":
                    pushIpWhitelistGrayConfig(plan, details);
                    break;
                case "USER_WHITELIST":
                    pushUserWhitelistGrayConfig(plan, details);
                    break;
                case "PERCENTAGE":
                    pushPercentageGrayConfig(plan, details);
                    break;
                case "CANARY":
                    pushCanaryGrayConfig(plan, details);
                    break;
                case "MANUAL":
                    pushManualGrayConfig(plan, details);
                    break;
                default:
                    throw new RuntimeException("不支持的灰度策略: " + strategy);
            }
            
            // 更新详情状态
            updateGrayDetailsStatus(planId, "GRAY", "灰度配置推送成功");
            
            log.info("灰度配置推送成功: planId={}, strategy={}", planId, strategy);
            
        } catch (Exception e) {
            log.error("推送灰度配置失败: planId={}", planId, e);
            // 更新详情状态为失败
            updateGrayDetailsStatus(planId, "FAILED", "灰度配置推送失败: " + e.getMessage());
            throw new RuntimeException("推送灰度配置失败: " + e.getMessage());
        }
    }

    /**
     * 回滚灰度配置
     */
    @Override
    public void rollbackGrayConfig(Long planId) {
        try {
            log.info("开始回滚灰度配置: planId={}", planId);
            
            // 获取灰度发布计划
            GrayReleasePlan plan = grayReleasePlanMapper.selectById(planId);
            if (plan == null) {
                throw new RuntimeException("灰度发布计划不存在: " + planId);
            }
            
            // 获取灰度发布详情
            List<GrayReleaseDetail> details = grayReleaseDetailMapper.selectByPlanId(planId);
            if (CollectionUtils.isEmpty(details)) {
                throw new RuntimeException("灰度发布详情为空: " + planId);
            }
            
            // 清理灰度配置节点
            cleanupGrayConfigNodes(plan);
            
            // 恢复原始配置
            for (GrayReleaseDetail detail : details) {
                try {
                    String zkPath = buildConfigPath(plan.getAppName(), plan.getEnvironment(), 
                                                  plan.getGroupName(), detail.getConfigKey());
                    
                    // 推送原始配置值
                    zooKeeperService.publishConfig(zkPath, detail.getOldValue());
                    
                    log.info("回滚配置成功: key={}, oldValue={}", detail.getConfigKey(), detail.getOldValue());
                    
                } catch (Exception e) {
                    log.error("回滚配置失败: key={}", detail.getConfigKey(), e);
                }
            }
            
            // 更新详情状态
            updateGrayDetailsStatus(planId, "ROLLBACK", "灰度配置回滚成功");
            
            // 通知实例刷新配置
            List<String> configKeys = details.stream()
                    .map(GrayReleaseDetail::getConfigKey)
                    .collect(Collectors.toList());
            notifyBatchConfigChange(plan.getAppName(), plan.getEnvironment(), configKeys);
            
            log.info("灰度配置回滚成功: planId={}", planId);
            
        } catch (Exception e) {
            log.error("回滚灰度配置失败: planId={}", planId, e);
            updateGrayDetailsStatus(planId, "ROLLBACK_FAILED", "灰度配置回滚失败: " + e.getMessage());
            throw new RuntimeException("回滚灰度配置失败: " + e.getMessage());
        }
    }

    /**
     * 通知指定实例刷新配置
     */
    @Override
    public void notifyInstanceRefresh(String appName, String environment, List<String> instanceIds) {
        try {
            if (CollectionUtils.isEmpty(instanceIds)) {
                log.warn("实例ID列表为空，跳过通知");
                return;
            }
            
            log.info("开始通知实例刷新配置: appName={}, environment={}, instances={}", 
                    appName, environment, instanceIds);
            
            // 创建通知消息
            Map<String, Object> notifyMessage = new HashMap<>();
            notifyMessage.put("action", "REFRESH_CONFIG");
            notifyMessage.put("appName", appName);
            notifyMessage.put("environment", environment);
            notifyMessage.put("timestamp", System.currentTimeMillis());
            notifyMessage.put("instanceIds", instanceIds);
            
            String message = objectMapper.writeValueAsString(notifyMessage);
            
            // 为每个实例创建通知节点
            for (String instanceId : instanceIds) {
                try {
                    String notifyPath = buildNotifyPath(appName, environment, instanceId);
                    zooKeeperService.createEphemeralNode(notifyPath, message);
                    log.debug("通知实例成功: instanceId={}, path={}", instanceId, notifyPath);
                } catch (Exception e) {
                    log.error("通知实例失败: instanceId={}", instanceId, e);
                }
            }
            
            // 创建全局通知节点
            String globalNotifyPath = buildGlobalNotifyPath(appName, environment);
            zooKeeperService.updateNode(globalNotifyPath, String.valueOf(System.currentTimeMillis()));
            
            log.info("实例刷新通知发送完成: count={}", instanceIds.size());
            
        } catch (Exception e) {
            log.error("通知实例刷新配置失败: appName={}, environment={}", appName, environment, e);
            throw new RuntimeException("通知实例刷新失败: " + e.getMessage());
        }
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 推送IP白名单灰度配置
     */
    private void pushIpWhitelistGrayConfig(GrayReleasePlan plan, List<GrayReleaseDetail> details) {
        try {
            Map<String, Object> grayRules = parseGrayRules(plan.getGrayRules());
            @SuppressWarnings("unchecked")
            List<String> ipWhitelist = (List<String>) grayRules.get("ipWhitelist");
            
            if (CollectionUtils.isEmpty(ipWhitelist)) {
                throw new RuntimeException("IP白名单为空");
            }
            
            log.info("推送IP白名单灰度配置: ips={}", ipWhitelist);
            
            for (GrayReleaseDetail detail : details) {
                for (String ip : ipWhitelist) {
                    String grayPath = buildGrayConfigPath(plan.getAppName(), plan.getEnvironment(), 
                                                        "ip-whitelist", ip, detail.getConfigKey());
                    zooKeeperService.publishConfig(grayPath, detail.getGrayValue());
                }
            }
            
        } catch (Exception e) {
            log.error("推送IP白名单灰度配置失败", e);
            throw new RuntimeException("推送IP白名单灰度配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 推送用户白名单灰度配置
     */
    private void pushUserWhitelistGrayConfig(GrayReleasePlan plan, List<GrayReleaseDetail> details) {
        try {
            Map<String, Object> grayRules = parseGrayRules(plan.getGrayRules());
            @SuppressWarnings("unchecked")
            List<String> userWhitelist = (List<String>) grayRules.get("userWhitelist");
            
            if (CollectionUtils.isEmpty(userWhitelist)) {
                throw new RuntimeException("用户白名单为空");
            }
            
            log.info("推送用户白名单灰度配置: users={}", userWhitelist);
            
            for (GrayReleaseDetail detail : details) {
                for (String userId : userWhitelist) {
                    String grayPath = buildGrayConfigPath(plan.getAppName(), plan.getEnvironment(), 
                                                        "user-whitelist", userId, detail.getConfigKey());
                    zooKeeperService.publishConfig(grayPath, detail.getGrayValue());
                }
            }
            
        } catch (Exception e) {
            log.error("推送用户白名单灰度配置失败", e);
            throw new RuntimeException("推送用户白名单灰度配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 推送按比例灰度配置
     */
    private void pushPercentageGrayConfig(GrayReleasePlan plan, List<GrayReleaseDetail> details) {
        try {
            log.info("推送按比例灰度配置: percentage={}", plan.getRolloutPercentage());
            
            for (GrayReleaseDetail detail : details) {
                String grayPath = buildGrayConfigPath(plan.getAppName(), plan.getEnvironment(), 
                                                    "percentage", null, detail.getConfigKey());
                
                // 创建灰度配置数据
                Map<String, Object> grayConfigData = new HashMap<>();
                grayConfigData.put("grayValue", detail.getGrayValue());
                grayConfigData.put("originalValue", detail.getOldValue());
                grayConfigData.put("percentage", plan.getRolloutPercentage());
                grayConfigData.put("strategy", "PERCENTAGE");
                grayConfigData.put("planId", plan.getId());
                
                String configData = objectMapper.writeValueAsString(grayConfigData);
                zooKeeperService.publishConfig(grayPath, configData);
            }
            
        } catch (Exception e) {
            log.error("推送按比例灰度配置失败", e);
            throw new RuntimeException("推送按比例灰度配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 推送金丝雀灰度配置
     */
    private void pushCanaryGrayConfig(GrayReleasePlan plan, List<GrayReleaseDetail> details) {
        try {
            Map<String, Object> grayRules = parseGrayRules(plan.getGrayRules());
            @SuppressWarnings("unchecked")
            Map<String, Object> canaryRules = (Map<String, Object>) grayRules.get("canary");
            
            if (canaryRules == null) {
                throw new RuntimeException("金丝雀规则为空");
            }
            
            @SuppressWarnings("unchecked")
            List<String> canaryInstances = (List<String>) canaryRules.get("canaryInstances");
            
            log.info("推送金丝雀灰度配置: instances={}", canaryInstances);
            
            for (GrayReleaseDetail detail : details) {
                if (!CollectionUtils.isEmpty(canaryInstances)) {
                    for (String instance : canaryInstances) {
                        String grayPath = buildGrayConfigPath(plan.getAppName(), plan.getEnvironment(), 
                                                            "canary", instance, detail.getConfigKey());
                        zooKeeperService.publishConfig(grayPath, detail.getGrayValue());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("推送金丝雀灰度配置失败", e);
            throw new RuntimeException("推送金丝雀灰度配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 推送手动选择实例灰度配置
     */
    private void pushManualGrayConfig(GrayReleasePlan plan, List<GrayReleaseDetail> details) {
        try {
            Map<String, Object> grayRules = parseGrayRules(plan.getGrayRules());
            @SuppressWarnings("unchecked")
            List<String> selectedInstances = (List<String>) grayRules.get("selectedInstances");
            
            if (CollectionUtils.isEmpty(selectedInstances)) {
                throw new RuntimeException("手动选择的实例列表为空");
            }
            
            log.info("推送手动选择实例灰度配置: instances={}", selectedInstances);
            
            for (GrayReleaseDetail detail : details) {
                for (String instance : selectedInstances) {
                    String grayPath = buildGrayConfigPath(plan.getAppName(), plan.getEnvironment(), 
                                                        "manual", instance, detail.getConfigKey());
                    zooKeeperService.publishConfig(grayPath, detail.getGrayValue());
                }
            }
            
        } catch (Exception e) {
            log.error("推送手动选择实例灰度配置失败", e);
            throw new RuntimeException("推送手动选择实例灰度配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理灰度配置节点
     */
    private void cleanupGrayConfigNodes(GrayReleasePlan plan) {
        try {
            String grayBasePath = String.format("/config-center/%s/%s/gray", 
                                               plan.getAppName(), plan.getEnvironment());
            
            // 删除所有灰度配置节点
            if (zooKeeperService.exists(grayBasePath)) {
                List<String> children = zooKeeperService.getChildren(grayBasePath);
                for (String child : children) {
                    String childPath = grayBasePath + "/" + child;
                    zooKeeperService.deleteConfig(childPath);
                }
            }
            
            log.info("清理灰度配置节点完成: basePath={}", grayBasePath);
            
        } catch (Exception e) {
            log.error("清理灰度配置节点失败", e);
        }
    }
    
    /**
     * 更新灰度详情状态
     */
    private void updateGrayDetailsStatus(Long planId, String status, String remark) {
        try {
            grayReleaseDetailMapper.updateStatusByPlanId(planId, status, remark);
        } catch (Exception e) {
            log.error("更新灰度详情状态失败: planId={}, status={}", planId, status, e);
        }
    }
    
    /**
     * 通知配置变更
     */
    private void notifyConfigChange(String appName, String environment, String configKey) {
        try {
            String notifyPath = buildConfigNotifyPath(appName, environment, configKey);
            String notifyData = String.valueOf(System.currentTimeMillis());
            zooKeeperService.updateNode(notifyPath, notifyData);
        } catch (Exception e) {
            log.error("通知配置变更失败: appName={}, environment={}, configKey={}", 
                     appName, environment, configKey, e);
        }
    }
    
    /**
     * 批量通知配置变更
     */
    private void notifyBatchConfigChange(String appName, String environment, List<String> configKeys) {
        try {
            for (String configKey : configKeys) {
                notifyConfigChange(appName, environment, configKey);
            }
            
            // 创建全局变更通知
            String globalNotifyPath = buildGlobalNotifyPath(appName, environment);
            String notifyData = String.valueOf(System.currentTimeMillis());
            zooKeeperService.updateNode(globalNotifyPath, notifyData);
            
        } catch (Exception e) {
            log.error("批量通知配置变更失败: appName={}, environment={}", appName, environment, e);
        }
    }
    
    /**
     * 解析灰度规则
     */
    private Map<String, Object> parseGrayRules(String grayRulesJson) {
        try {
            if (!StringUtils.hasText(grayRulesJson)) {
                return new HashMap<>();
            }
            return objectMapper.readValue(grayRulesJson, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.error("解析灰度规则失败: {}", grayRulesJson, e);
            return new HashMap<>();
        }
    }
    
    /**
     * 构建配置路径
     */
    private String buildConfigPath(String appName, String environment, String groupName, String configKey) {
        if (StringUtils.hasText(groupName)) {
            return String.format("/config-center/%s/%s/%s/%s", appName, environment, groupName, configKey);
        } else {
            return String.format("/config-center/%s/%s/%s", appName, environment, configKey);
        }
    }
    
    /**
     * 构建灰度配置路径
     */
    private String buildGrayConfigPath(String appName, String environment, String strategy, String target, String configKey) {
        if (target != null) {
            return String.format("/config-center/%s/%s/gray/%s/%s/%s", 
                               appName, environment, strategy, target, configKey);
        } else {
            return String.format("/config-center/%s/%s/gray/%s/%s", 
                               appName, environment, strategy, configKey);
        }
    }
    
    /**
     * 构建通知路径
     */
    private String buildNotifyPath(String appName, String environment, String instanceId) {
        return String.format("/config-center/%s/%s/notify/instances/%s", appName, environment, instanceId);
    }
    
    /**
     * 构建全局通知路径
     */
    private String buildGlobalNotifyPath(String appName, String environment) {
        return String.format("/config-center/%s/%s/notify/global", appName, environment);
    }
    
    /**
     * 构建配置通知路径
     */
    private String buildConfigNotifyPath(String appName, String environment, String configKey) {
        return String.format("/config-center/%s/%s/notify/configs/%s", appName, environment, configKey);
    }
}