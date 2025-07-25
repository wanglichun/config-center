package com.example.configcenter.service;

import com.example.configcenter.dto.TicketCreateDto;
import com.example.configcenter.entity.Ticket;

/**
 * 工单服务接口
 */
public interface TicketService {
    
    /**
     * 创建工单
     * @param req 工单信息
     * @return 创建的工单ID
     */
    Long createTicket(TicketCreateDto req);

    Ticket getTicket(Long id);
}