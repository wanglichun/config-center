package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 工单控制器
 */
@RestController
@RequestMapping("/api/ticket")
@Slf4j
public class TicketController {
    
    @Autowired
    private TicketService ticketService;
    
    /**
     * 分页查询工单列表
     */
    @GetMapping("/page")
    public ApiResult<PageResult<Ticket>> getTicketPage(TicketQueryRequest request) {
        try {
            log.info("查询工单列表，参数：{}", request);
            PageResult<Ticket> result = ticketService.getTicketPage(request);
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("查询工单列表失败", e);
            return ApiResult.error("查询工单列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取工单详情
     */
    @GetMapping("/{id}")
    public ApiResult<Ticket> getTicketById(@PathVariable Long id) {
        try {
            log.info("获取工单详情，ID：{}", id);
            Ticket ticket = ticketService.getTicketById(id);
            return ApiResult.success(ticket);
        } catch (Exception e) {
            log.error("获取工单详情失败", e);
            return ApiResult.error("获取工单详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 创建工单
     */
    @PostMapping
    public ApiResult<Long> createTicket(@RequestBody Ticket ticket) {
        try {
            log.info("创建工单，参数：{}", ticket);
            Long ticketId = ticketService.createTicket(ticket);
            return ApiResult.success(ticketId);
        } catch (Exception e) {
            log.error("创建工单失败", e);
            return ApiResult.error("创建工单失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新工单
     */
    @PutMapping("/{id}")
    public ApiResult<Boolean> updateTicket(@PathVariable Long id, @RequestBody Ticket ticket) {
        try {
            log.info("更新工单，ID：{}，参数：{}", id, ticket);
            ticket.setId(id);
            boolean result = ticketService.updateTicket(ticket);
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("更新工单失败", e);
            return ApiResult.error("更新工单失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除工单
     */
    @DeleteMapping("/{id}")
    public ApiResult<Boolean> deleteTicket(@PathVariable Long id) {
        try {
            log.info("删除工单，ID：{}", id);
            boolean result = ticketService.deleteTicket(id);
            return ApiResult.success(result);
        } catch (Exception e) {
            log.error("删除工单失败", e);
            return ApiResult.error("删除工单失败：" + e.getMessage());
        }
    }
}
