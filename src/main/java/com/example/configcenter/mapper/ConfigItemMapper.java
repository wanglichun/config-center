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
    ConfigItem findByKey(@Param("groupName") String groupName,
                        @Param("configKey") String configKey);


    /**
     * 搜索配置项
     */
    List<ConfigItem> search(ConfigQueryDto queryDto);

    /**
     * 统计配置项总数
     */
    long countByQuery(ConfigQueryDto queryDto);

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
} 