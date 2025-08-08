package com.example.configcenter.service;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.dto.TicketUpdateRequest;
import com.example.configcenter.entity.Ticket;

/**
 * 工单服务接口
 */
public interface TicketService {
    
    /**
     * 分页查询工单列表
     */
    PageResult<Ticket> getTicketPage(TicketQueryRequest request);
    
    /**
     * 根据ID获取工单
     */
    Ticket getTicketById(Long id);
    
    /**
     * 创建工单
     */
    Long createTicket(Ticket ticket);
    
    /**
     * 更新工单
     */
    Ticket updateTicket(Long id, TicketUpdateRequest ticketUpdateRequest);
    
    /**
     * 删除工单
     */
    boolean deleteTicket(Long id);

    /**
     * 获取配置为结束的工单
     */
    Ticket getTicketByConfigId(Long id);
}
