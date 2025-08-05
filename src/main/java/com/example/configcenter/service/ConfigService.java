package com.example.configcenter.service;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.ConfigQueryDto;
import com.example.configcenter.dto.PublishDto;
import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.entity.ConfigHistory;
import com.example.configcenter.entity.ConfigHistoryReq;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.Ticket;

import java.util.List;
import java.util.Map;

/**
 * 配置服务接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public interface ConfigService {

    /**
     * 获取配置项
     *
     * @param id 配置id
     * @return 配置项
     */
    ConfigItem getConfig(Long id);
    /**
     * 创建配置项
     *
     * @param configItem 配置项
     * @return 是否成功
     */
    boolean createConfig(ConfigItem configItem);

    /**
     * 更新配置项
     *
     * @param configItem 配置项
     * @return 是否成功
     */
    Ticket updateConfig(ConfigItem configItem);

    /**
     * 删除配置项
     *
     * @param id 配置项ID
     * @return 是否成功
     */
    boolean deleteConfig(Long id);

    /**
     * 发布配置到ZooKeeper
     *
     * @param id 配置项ID
     * @param ipList 发布机器列表
     * @return 是否成功
     */
    boolean publishConfig(Long id, PublishDto publishDto);

    /**
     * 回滚配置
     *
     * @param id 配置项ID
     * @param targetVersion 目标版本
     * @param operator 操作者
     * @return 是否成功
     */
    boolean rollbackConfig(Long id, Long targetVersion);

    /**
     * 获取配置历史
     *
     * @param request
     * @return 历史记录列表
     */
    PageResult<Ticket> getConfigHistory(TicketQueryRequest request);

    /**
     * 分页查询配置项
     *
     * @param queryDto 查询参数
     * @return 分页结果
     */
    List<ConfigItem> getConfigPage(ConfigQueryDto queryDto);

    /**
     * 获取配置项总数
     *
     * @param queryDto 查询参数
     * @return 总数
     */
    long getConfigCount(ConfigQueryDto queryDto);

    /**
     * 检查配置格式
     *
     * @param configValue 配置值
     * @param dataType 数据类型
     * @return 是否有效
     */
    boolean validateConfig(String configValue, String dataType);

}