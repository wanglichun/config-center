package com.example.configcenter.mapper;

import com.example.configcenter.entity.ConfigHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 配置历史Mapper接口
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Mapper
public interface ConfigHistoryMapper {

    /**
     * 根据ID查询配置历史
     */
    ConfigHistory findById(@Param("id") Long id);

    /**
     * 根据配置ID查询历史记录
     */
    List<ConfigHistory> findByConfigId(@Param("configId") Long configId);

    /**
     * 根据配置ID和版本查询历史记录
     */
    ConfigHistory findByConfigAndVersion(@Param("configId") Long configId, 
                                       @Param("version") Long version);

    /**
     * 根据操作者查询历史记录
     */
    List<ConfigHistory> findByOperator(@Param("operator") String operator,
                                      @Param("startTime") Long startTime,
                                      @Param("endTime") Long endTime);

    /**
     * 根据应用查询历史记录
     */
    List<ConfigHistory> findByApp(@Param("appName") String appName,
                                 @Param("environment") String environment,
                                 @Param("startTime") Long startTime,
                                 @Param("endTime") Long endTime);

    /**
     * 分页查询历史记录
     */
    List<ConfigHistory> findByPage(@Param("offset") int offset,
                                  @Param("limit") int limit,
                                  @Param("appName") String appName,
                                  @Param("environment") String environment);

    /**
     * 统计历史记录数量
     */
    int countByApp(@Param("appName") String appName,
                   @Param("environment") String environment);

    /**
     * 插入历史记录
     */
    int insert(ConfigHistory configHistory);

    /**
     * 批量插入历史记录
     */
    int batchInsert(@Param("list") List<ConfigHistory> configHistories);

    /**
     * 删除过期的历史记录
     */
    int deleteExpired(@Param("expireTime") Long expireTime);

    /**
     * 根据操作类型统计
     */
    List<ConfigHistory> findByOperationType(@Param("operationType") String operationType,
                                           @Param("startTime") Long startTime,
                                           @Param("endTime") Long endTime);
} 