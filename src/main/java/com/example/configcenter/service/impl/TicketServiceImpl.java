package com.example.configcenter.service.impl;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.mapper.TicketMapper;
import com.example.configcenter.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工单服务实现类
 */
@Service
@Slf4j
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;

    @Override
    public PageResult<Ticket> getTicketPage(TicketQueryRequest request) {
        try {
            // 计算偏移量
            int offset = (request.getPageNum() - 1) * request.getPageSize();
            
            // 查询工单列表
            List<Ticket> tickets = ticketMapper.findByPage(offset, request.getPageSize(), 
                request.getTitle(), request.getPhase(), request.getApplicator());
            
            // 查询总数量
            int total = ticketMapper.count(request.getTitle(), request.getPhase(), request.getApplicator());
            
            return new PageResult<>(tickets, total, request.getPageNum(), request.getPageSize());
            
        } catch (Exception e) {
            log.error("查询工单列表失败", e);
            throw new RuntimeException("查询工单列表失败：" + e.getMessage());
        }
    }

    @Override
    public Ticket getTicketById(Long id) {
        if (id == null) {
            throw new RuntimeException("工单ID不能为空");
        }
        
        Ticket ticket = ticketMapper.findById(id);
        if (ticket == null) {
            throw new RuntimeException("工单不存在");
        }
        
        return ticket;
    }

    @Override
    public Long createTicket(Ticket ticket) {
        try {
            int result = ticketMapper.create(ticket);
            return result > 0 ? ticket.getId() : null;
        } catch (Exception e) {
            log.error("创建工单失败", e);
            throw new RuntimeException("创建工单失败：" + e.getMessage());
        }
    }

    @Override
    public boolean updateTicket(Ticket ticket) {
        try {
//            ticket.setUpdateTime(LocalDateTime.now());
            int result = ticketMapper.update(ticket);
            return result > 0;
        } catch (Exception e) {
            log.error("更新工单失败", e);
            throw new RuntimeException("更新工单失败：" + e.getMessage());
        }
    }

    @Override
    public boolean deleteTicket(Long id) {
        try {
            int result = ticketMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("删除工单失败", e);
            throw new RuntimeException("删除工单失败：" + e.getMessage());
        }
    }
}
