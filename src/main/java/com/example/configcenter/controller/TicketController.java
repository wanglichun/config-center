package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.dto.TicketCreateDto;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单控制器
 */
@RestController
@RequestMapping("/api/ticket")
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    /**
     * 创建工单
     */
    @PostMapping
    public ApiResult<Long> createTicket(@RequestBody TicketCreateDto req) {
        Long ticketId = ticketService.createTicket(req);
        return ApiResult.success(ticketId);
    }
    
    /**
     * 获取工单详情
     */
    @GetMapping("/{id}")
    public ApiResult<Ticket> getTicket(@PathVariable Long id) {
        Ticket ticket = ticketService.getTicket(id);
        return ApiResult.success(ticket);
    }
}
