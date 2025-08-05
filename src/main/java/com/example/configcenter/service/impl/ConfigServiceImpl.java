package com.example.configcenter.service.impl;

import com.alibaba.druid.support.json.JSONUtils;
import com.alibaba.fastjson.JSON;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.context.Context;
import com.example.configcenter.context.ContextManager;
import com.example.configcenter.dto.ConfigQueryDto;
import com.example.configcenter.dto.PublishDto;
import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.entity.ConfigHistory;
import com.example.configcenter.entity.ConfigHistoryReq;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.enums.TicketPhaseEnum;
import com.example.configcenter.mapper.ConfigHistoryMapper;
import com.example.configcenter.mapper.ConfigItemMapper;
import com.example.configcenter.service.ConfigService;
import com.example.configcenter.service.MachineService;
import com.example.configcenter.service.TicketService;
import com.example.configcenter.service.ZooKeeperService;
import com.example.configcenter.utils.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 配置服务实现类
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class ConfigServiceImpl implements ConfigService {



    @Autowired
    private ConfigItemMapper configItemMapper;

    @Autowired
    private ConfigHistoryMapper configHistoryMapper;

    @Autowired
    private MachineService machineConfigSubscriptionService;

    @Autowired
    TicketService ticketService;

    @Override
    @Cacheable(value = "config", key = "#groupName + ':' + #configKey")
    public ConfigItem getConfig(Long id) {
        try {
            return configItemMapper.findById(id);
        } catch (Exception e) {
            log.error("获取配置失败: id={}, configKey={}", id, e);
            return null;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean createConfig(ConfigItem configItem) {
        try {
            // 设置基础信息
            configItem.setVersion(System.currentTimeMillis());
            configItem.setStatus("ACTIVE");
            configItem.setCreateTime(System.currentTimeMillis());
            configItem.setUpdateTime(System.currentTimeMillis());
            configItem.setDelFlag(0);
            // 生成ZK路径
            String zkPath = buildZkPath(configItem.getGroupName(), configItem.getConfigKey());
            configItem.setZkPath(zkPath);

            // 保存到数据库
            int result = configItemMapper.insert(configItem);

            return true;
        } catch (Exception e) {
            log.error("创建配置项失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public Ticket updateConfig(ConfigItem configItem) {
        try {
            ConfigItem oldConfig = configItemMapper.findById(configItem.getId());
            if (oldConfig == null) {
                throw new IllegalArgumentException("配置项不存在");
            }

            // 版本号递增
            configItem.setVersion(System.currentTimeMillis());
            configItem.setUpdateTime(System.currentTimeMillis());

            Ticket ticket = buildTicket(oldConfig, configItem);
            ticketService.createTicket(ticket);

            return ticket;
        } catch (Exception e) {
            log.error("更新配置项失败", e);
            return null;
        }
    }

    private Ticket buildTicket(ConfigItem oldConfig, ConfigItem newConfig) {
        newConfig.setVersion(System.currentTimeMillis());
        Ticket ticket = new Ticket();
        Context context = ContextManager.getContext();
        ticket.setTitle(newConfig.getConfigKey());
        ticket.setOldData(JsonUtil.objectToString(oldConfig));
        ticket.setNewData(JsonUtil.objectToString(newConfig));
        ticket.setApplicator(context.getUserEmail());
        ticket.setDataId(oldConfig.getId());
        ticket.setPhase(TicketPhaseEnum.Reviewing);
        return ticket;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean deleteConfig(Long id) {
        try {
            ConfigItem configItem = configItemMapper.findById(id);
            if (configItem == null) {
                log.warn("配置项不存在: {}", id);
                return false;
            }

            // 软删除
            configItem.setDelFlag(1);
            configItem.setUpdateTime(System.currentTimeMillis());

            int result = configItemMapper.deleteById(configItem.getId());


            return true;
        } catch (Exception e) {
            log.error("删除配置项失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean publishConfig(Long id, PublishDto publishDto) {
        try {
            Ticket ticket = ticketService.getTicketById(publishDto.getTicketId());
            ConfigItem configItem;
            if ("Publish".equals(publishDto.getAction())) {
                configItem = JsonUtil.jsonToObject(ticket.getNewData(), ConfigItem.class);
            } else {
                configItem = JsonUtil.jsonToObject(ticket.getOldData(), ConfigItem.class);
            }
            // 通知订阅的机器配置变更
            int notifiedCount = machineConfigSubscriptionService.notifyConfigChange(
                    configItem.getGroupName(), configItem.getConfigKey(), configItem.getConfigValue(), configItem.getVersion(), publishDto.getIpList());
            log.info("配置发布成功: {}, 通知机器数: {}", configItem.getConfigKey(), notifiedCount);
            return true;
        } catch (
                Exception e) {
            log.error("发布配置失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = {"config", "configs", "configMap"}, allEntries = true)
    public boolean rollbackConfig(Long id, Long targetVersion) {
        try {
            String operator = ContextManager.getContext().getUserEmail();
            ConfigItem currentConfig = configItemMapper.findById(id);
            if (currentConfig == null) {
                log.warn("配置项不存在: {}", id);
                return false;
            }

            ConfigHistory targetHistory = configHistoryMapper.findByConfigAndVersion(id, targetVersion);
            if (targetHistory == null) {
                log.warn("目标版本不存在: configId={}, version={}", id, targetVersion);
                return false;
            }

            // 回滚配置值
            currentConfig.setConfigValue(targetHistory.getNewValue());
            currentConfig.setVersion(currentConfig.getVersion() + 1);
            currentConfig.setUpdateTime(System.currentTimeMillis());
            currentConfig.setPublisher(operator);

            int result = configItemMapper.update(currentConfig);

            if (result > 0) {


                log.info("配置回滚成功: configId={}, targetVersion={}", id, targetVersion);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("回滚配置失败", e);
            return false;
        }
    }

    @Override
    public PageResult<Ticket> getConfigHistory(TicketQueryRequest req) {
        return ticketService.getTicketPage(req);
    }

    @Override
    public List<ConfigItem> getConfigPage(ConfigQueryDto queryDto) {
        try {
            // 将页码转换为偏移量 (pageNum从1开始，需要转换为从0开始的偏移量)
            int offset = (queryDto.getPageNum() - 1) * queryDto.getPageSize();
            queryDto.setPageNum(offset);
            return configItemMapper.search(queryDto);
        } catch (Exception e) {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public long getConfigCount(ConfigQueryDto queryDto) {
        try {
            return configItemMapper.countByQuery(queryDto);
        } catch (Exception e) {
            log.error("获取配置总数失败", e);
            return 0;
        }
    }


    @Override
    public boolean validateConfig(String configValue, String dataType) {
        if (!StringUtils.hasText(configValue)) {
            return false;
        }

        try {
            switch (dataType.toUpperCase()) {
                case "NUMBER":
                    Double.parseDouble(configValue);
                    break;
                case "BOOLEAN":
                    if (!"true".equalsIgnoreCase(configValue) && !"false".equalsIgnoreCase(configValue)) {
                        return false;
                    }
                    break;
                case "JSON":
                    JSON.parseObject(configValue);
                    break;
                case "STRING":
                default:
                    // 字符串类型不需要特殊验证
                    break;
            }
            return true;
        } catch (Exception e) {
            log.warn("配置格式验证失败: value={}, type={}", configValue, dataType, e);
            return false;
        }
    }


    /**
     * 构建ZooKeeper路径
     */
    private String buildZkPath(String groupName, String configKey) {
        return String.format("/configs/%s/%s", groupName, configKey);
    }
} 