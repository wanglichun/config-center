package com.example.configcenter.mapper;

import com.example.configcenter.entity.Ticket;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

/**
 * 工单Mapper接口
 */
@Mapper
public interface TicketMapper {

    /**
     * 创建ticket
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int create(Ticket ticket);

    /**
     * 根据id获取ticket
     */
    Ticket get(Long id);
} 