package com.example.configcenter.service;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.ConfigQueryDto;
import com.example.configcenter.entity.ConfigHistory;
import com.example.configcenter.entity.ConfigItem;
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
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 配置组
     * @param configKey 配置键
     * @return 配置项
     */
    ConfigItem getConfig(String appName, String environment, String groupName, String configKey);

    /**
     * 获取应用的所有配置
     *
     * @param appName 应用名称
     * @param environment 环境
     * @return 配置项列表
     */
    List<ConfigItem> getConfigs(String appName, String environment);

    /**
     * 获取应用配置的键值对
     *
     * @param appName 应用名称
     * @param environment 环境
     * @param groupName 配置组
     * @return 配置键值对
     */
    Map<String, String> getConfigMap(String appName, String environment, String groupName);

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
    boolean updateConfig(ConfigItem configItem);

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
     * @param publisher 发布者
     * @return 是否成功
     */
    boolean publishConfig(Long id, String publisher);

    /**
     * 批量发布配置
     *
     * @param appName 应用名称
     * @param environment 环境
     * @param publisher 发布者
     * @return 是否成功
     */
    boolean publishConfigs(String appName, String environment, String publisher);

    /**
     * 回滚配置
     *
     * @param id 配置项ID
     * @param targetVersion 目标版本
     * @param operator 操作者
     * @return 是否成功
     */
    boolean rollbackConfig(Long id, Long targetVersion, String operator);

    /**
     * 获取配置历史
     *
     * @param configId 配置项ID
     * @return 历史记录列表
     */
    List<ConfigHistory> getConfigHistory(Long configId);

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
     * 导入配置
     *
     * @param configItems 配置项列表
     * @param operator 操作者
     * @return 成功导入的数量
     */
    int importConfigs(List<ConfigItem> configItems, String operator);

    /**
     * 导出配置
     *
     * @param appName 应用名称
     * @param environment 环境
     * @return 配置项列表
     */
    List<ConfigItem> exportConfigs(String appName, String environment);

    /**
     * 检查配置格式
     *
     * @param configValue 配置值
     * @param dataType 数据类型
     * @return 是否有效
     */
    boolean validateConfig(String configValue, String dataType);

    /**
     * 加密配置值
     *
     * @param configValue 原始配置值
     * @return 加密后的配置值
     */
    String encryptConfig(String configValue);

    /**
     * 解密配置值
     *
     * @param encryptedValue 加密的配置值
     * @return 解密后的配置值
     */
    String decryptConfig(String encryptedValue);
}