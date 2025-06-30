package com.example.configcenter.mapper;

import com.example.configcenter.entity.GrayReleaseDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 灰度发布详情Mapper接口
 */
@Mapper
public interface GrayReleaseDetailMapper {
    
    /**
     * 插入灰度发布详情
     */
    int insert(GrayReleaseDetail detail);
    
    /**
     * 批量插入
     */
    int batchInsert(@Param("details") List<GrayReleaseDetail> details);
    
    /**
     * 根据ID查询
     */
    GrayReleaseDetail selectById(Long id);
    
    /**
     * 根据计划ID查询详情列表
     */
    List<GrayReleaseDetail> selectByPlanId(@Param("planId") Long planId);
    
    /**
     * 更新详情
     */
    int updateById(GrayReleaseDetail detail);
    
    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, 
                     @Param("status") String status, 
                     @Param("updateBy") String updateBy);
    
    /**
     * 根据计划ID更新所有详情状态
     */
    int updateStatusByPlanId(@Param("planId") Long planId, 
                             @Param("status") String status, 
                             @Param("updateBy") String updateBy);
    
    /**
     * 查询配置的灰度详情
     */
    GrayReleaseDetail selectActiveByConfig(@Param("appName") String appName,
                                           @Param("environment") String environment,
                                           @Param("configKey") String configKey);
} 