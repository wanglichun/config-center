package com.example.configcenter.service.impl;

import com.example.configcenter.common.PageResult;
import com.example.configcenter.context.Context;
import com.example.configcenter.context.ContextManager;
import com.example.configcenter.dto.TicketQueryRequest;
import com.example.configcenter.dto.TicketUpdateRequest;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.Ticket;
import com.example.configcenter.enums.TicketActionEnum;
import com.example.configcenter.mapper.ConfigItemMapper;
import com.example.configcenter.mapper.TicketMapper;
import com.example.configcenter.service.ConfigService;
import com.example.configcenter.service.TicketService;
import com.example.configcenter.service.ZooKeeperService;
import com.example.configcenter.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    private ConfigItemMapper configItemMapper;
    @Autowired
    private ZooKeeperService zooKeeperService;

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
    @Transactional(rollbackFor = Exception.class)
    public Ticket updateTicket(Long id, TicketUpdateRequest ticketUpdateRequest) {
        Context context = ContextManager.getContext();
        Ticket ticket = getTicketById(id);
        ticket.setOperator(context.getUserEmail());
        ticket.setUpdateTime(System.currentTimeMillis());
        ticket.setPhase(TicketActionEnum.getTargetPhase(ticketUpdateRequest.getAction()));
        ticketMapper.update(ticket);

        String newData = ticket.getNewData();
        ConfigItem configItem = JsonUtil.jsonToObject(newData, ConfigItem.class);

        switch (ticketUpdateRequest.getAction()) {
            case Complete: {
                // 根据mysql配置数据
                configItemMapper.update(configItem);
                // 更新zk存储数据
                zooKeeperService.publishConfig(configItem.getZkPath(), configItem.getConfigValue());
            }
        }
        return ticket;
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
