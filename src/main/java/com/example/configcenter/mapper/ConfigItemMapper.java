package com.example.configcenter.mapper;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.ConfigQueryDto;
import com.example.configcenter.entity.ConfigItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配置项Mapper接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Mapper
public interface ConfigItemMapper {

    /**
     * 根据ID查询配置项
     */
    ConfigItem findById(@Param("id") Long id);

    /**
     * 根据配置键查询配置项
     */
    ConfigItem findByKey(@Param("appName") String appName, 
                        @Param("environment") String environment,
                        @Param("groupName") String groupName, 
                        @Param("configKey") String configKey);

    /**
     * 根据配置键查询配置项（不指定组名）
     */
    ConfigItem findByKeyWithoutGroup(@Param("appName") String appName, 
                                   @Param("environment") String environment,
                                   @Param("configKey") String configKey);

    /**
     * 根据应用查询配置项列表
     */
    List<ConfigItem> findByApp(@Param("appName") String appName, 
                              @Param("environment") String environment);

    /**
     * 根据配置组查询配置项列表
     */
    List<ConfigItem> findByGroup(@Param("appName") String appName, 
                                @Param("environment") String environment,
                                @Param("groupName") String groupName);

    /**
     * 搜索配置项
     */
    List<ConfigItem> search(ConfigQueryDto queryDto);

    /**
     * 分页查询配置项
     */
    List<ConfigItem> findByPage(@Param("offset") int offset,
                               @Param("limit") int limit,
                               @Param("appName") String appName,
                               @Param("environment") String environment);

    /**
     * 统计配置项数量
     */
    int countByApp(@Param("appName") String appName, 
                   @Param("environment") String environment);

    /**
     * 插入配置项
     */
    int insert(ConfigItem configItem);

    /**
     * 更新配置项
     */
    int update(ConfigItem configItem);

    /**
     * 根据ID删除配置项
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量插入配置项
     */
    int batchInsert(@Param("list") List<ConfigItem> configItems);

    /**
     * 批量更新配置项
     */
    int batchUpdate(@Param("list") List<ConfigItem> configItems);

    /**
     * 获取所有应用名称
     */
    List<String> findAllAppNames();

    /**
     * 获取应用的所有环境
     */
    List<String> findEnvironmentsByApp(@Param("appName") String appName);

    /**
     * 获取应用环境的所有配置组
     */
    List<String> findGroupsByAppEnv(@Param("appName") String appName, 
                                   @Param("environment") String environment);
} 