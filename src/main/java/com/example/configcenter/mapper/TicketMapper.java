package com.example.configcenter.mapper;

import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.enums.TicketPhaseEnum;
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
    List<Ticket> findByPage(TicketQueryRequest ticketQueryRequest);

    /**
     * 统计工单总数
     */
    int count(TicketQueryRequest ticketQueryRequest);

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