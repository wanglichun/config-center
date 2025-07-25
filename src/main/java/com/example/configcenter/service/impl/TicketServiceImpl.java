package com.example.configcenter.service.impl;

import com.example.configcenter.dto.TicketCreateDto;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.enums.TicketPhaseEnum;
import com.example.configcenter.mapper.TicketMapper;
import com.example.configcenter.service.ConfigService;
import com.example.configcenter.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 工单服务实现类
 */
@Service
@Slf4j
public class TicketServiceImpl implements TicketService {

    @Autowired
    private TicketMapper ticketMapper;
    
    @Autowired
    private ConfigService configService;

    @Override
    public Long createTicket(TicketCreateDto req) {
        try {
            ConfigItem config = configService.getConfig(req.getDataId());
            Ticket ticket = buildTicket(req, config);
            int result = ticketMapper.create(ticket);
            return result > 0 ? ticket.getId() : null;
        } catch (Exception e) {
            log.error("创建Ticket失败", e);
            return null;
        }
    }

    @Override
    public Ticket getTicket(Long id) {
        return ticketMapper.get(id);
    }

    private Ticket buildTicket(TicketCreateDto req, ConfigItem config) {
        Ticket ticket = new Ticket();
        ticket.setTitle(req.getTitle());
        ticket.setDataId(req.getDataId());
        ticket.setApplicator("admin"); // 暂时使用固定值
        ticket.setOldData(config.getConfigValue());
        ticket.setNewData(req.getNewData());
        ticket.setCreateTime(System.currentTimeMillis());
        ticket.setUpdateTime(System.currentTimeMillis());
        ticket.setPhase(TicketPhaseEnum.PENDING.getCode());
        return ticket;
    }
} 