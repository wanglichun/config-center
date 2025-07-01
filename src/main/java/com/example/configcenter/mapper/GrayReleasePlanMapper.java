package com.example.configcenter.mapper;

import com.example.configcenter.entity.GrayReleasePlan;
import com.example.configcenter.dto.GrayReleaseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 灰度发布计划Mapper接口
 */
@Mapper
public interface GrayReleasePlanMapper {
    
    /**
     * 插入灰度发布计划
     */
    int insert(GrayReleasePlan plan);
    
    /**
     * 根据ID查询
     */
    GrayReleasePlan selectById(Long id);
    
    /**
     * 更新灰度发布计划
     */
    int updateById(GrayReleasePlan plan);
    
    /**
     * 根据ID删除
     */
    int deleteById(Long id);
    
    /**
     * 分页查询灰度发布计划
     */
    List<GrayReleasePlan> selectByPage(@Param("query") GrayReleaseDto.PlanQueryRequest query);
    
    /**
     * 查询总数
     */
    int countByQuery(@Param("query") GrayReleaseDto.PlanQueryRequest query);
    
    /**
     * 查询进行中的灰度发布计划
     */
    List<GrayReleasePlan> selectActiveByConfig(@Param("appName") String appName,
                                               @Param("environment") String environment,
                                               @Param("configKey") String configKey);
    
    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, 
                     @Param("status") String status, 
                     @Param("updateBy") String updateBy);
} 