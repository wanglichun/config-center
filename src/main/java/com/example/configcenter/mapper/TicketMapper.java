package com.example.configcenter.mapper;

import com.example.configcenter.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 工单Mapper接口
 */
@Mapper
public interface TicketMapper {

    /**
     * 分页查询工单列表
     */
    List<Ticket> findByPage(@Param("offset") int offset,
                            @Param("pageSize") int pageSize,
                            @Param("title") String title,
                            @Param("phase") String phase,
                            @Param("applicator") String applicator,
                            @Param("configId") Long configId);

    /**
     * 统计工单总数
     */
    int count(@Param("title") String title,
              @Param("phase") String phase,
              @Param("applicator") String applicator,
              @Param("configId") Long configId);

    /**
     * 根据ID获取工单
     */
    Ticket findById(Long id);

    /**
     * 创建工单
     */
    int create(Ticket ticket);

    /**
     * 更新工单
     */
    int update(Ticket ticket);

    /**
     * 删除工单
     */
    int deleteById(Long id);
}