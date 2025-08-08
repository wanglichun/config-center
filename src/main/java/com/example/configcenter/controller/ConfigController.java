package com.example.configcenter.controller;

import com.alibaba.fastjson.JSON;
import com.example.configcenter.common.ApiResult;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.dto.*;
import com.example.configcenter.entity.*;
import com.example.configcenter.service.ConfigService;
import com.example.configcenter.service.MachineService;
import com.example.configcenter.service.TicketService;
import com.example.configcenter.service.ZooKeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 配置管理控制器
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    @Autowired
    private ConfigService configService;
    @Autowired
    private MachineService machineService;
    @Autowired
    private ZooKeeperService zooKeeperService;
    @Autowired
    TicketService ticketService;

    /**
     * 创建配置
     */
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> createConfig(@RequestBody ConfigCreateReq configCreateReq) {
        try {
            boolean result = configService.createConfig(configCreateReq);
            return result ? ApiResult.success(true) : ApiResult.error("编辑配置失败");
        } catch (Exception e) {
            return ApiResult.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个配置
     */
    @GetMapping("/{id}")
    public ApiResult<ConfigItem> getConfig(@PathVariable Long id) {
        return ApiResult.success(configService.getConfig(id));
    }

    /**
     * 分页查询配置
     */
    @GetMapping("/page")
    public ApiResult<PageResult<ConfigItem>> getConfigPage(ConfigQueryDto req) {
        try {
            // 执行分页查询
            List<ConfigItem> pageResult = configService.getConfigPage(req);
            // 获取总记录数
            long total = configService.getConfigCount(req);
            return ApiResult.success(new PageResult<>(pageResult, total, req.getPageNum(), req.getPageSize()));
        } catch (Exception e) {
            log.error("分页查询配置失败", e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> deleteConfig(@PathVariable Long id, HttpServletRequest request) {
        boolean result = configService.deleteConfig(id);
        return result ? ApiResult.success(true) : ApiResult.error("删除配置失败");
    }

    /**
     * 编辑配置
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Ticket> editConfig(@PathVariable Long id,
                                        @RequestBody ConfigUpdateReq configUpdateReq,
                                        HttpServletRequest request) {
        try {
            Ticket ticket = configService.updateConfig(configUpdateReq);
            return ApiResult.success(ticket);
        } catch (Exception e) {
            return ApiResult.error("编辑失败：" + e.getMessage());
        }
    }

    /**
     * 发布配置
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> publishConfig(@PathVariable Long id,
                                            @RequestBody PublishDto publishDto,
                                            HttpServletRequest request) {
        boolean result = configService.publishConfig(id, publishDto);
        return result ? ApiResult.success(true) : ApiResult.error("发布配置失败");
    }


    /**
     * 回滚配置
     */
    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> rollbackConfig(@PathVariable Long id,
                                            @RequestParam Long targetVersion,
                                            HttpServletRequest request) {
        boolean result = configService.rollbackConfig(id, targetVersion);
        return result ? ApiResult.success(true) : ApiResult.error("回滚配置失败");
    }

    /**
     * 验证配置格式
     */
    @PostMapping("/validate")
    public ApiResult<Boolean> validateConfig(@RequestParam String configValue,
                                            @RequestParam String dataType) {
        boolean valid = configService.validateConfig(configValue, dataType);
        return ApiResult.success(valid);
    }

    /**
     * 获取订阅指定配置的容器列表
     */
    @GetMapping("/{id}/subscribers")
    public ApiResult<List<MachineInstance>> getSubscribedContainers(@PathVariable Long id,
                                                                    @RequestParam Long ticketId) {
        try {
            log.info("查询订阅机器列表: id={}", id);
            Ticket ticket = ticketService.getTicketById(ticketId);
            ConfigItem config = JSON.parseObject(ticket.getNewData(), ConfigItem.class);
            List<MachineInstance> subscribedMachines = machineService.getSubscribedMachines(config.getGroupName(), config.getConfigKey());
            for (MachineInstance machineInstance : subscribedMachines) {
                if (config.getVersion().equals(machineInstance.getVersion())) {
                    machineInstance.setStatus("Success");
                } else {
                    machineInstance.setStatus("NoGray");
                }
            }
            return ApiResult.success(subscribedMachines);
        } catch (Exception e) {
            log.error("获取订阅机器列表失败: id={}", id, e);
            return ApiResult.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ApiResult<PageResult<Ticket>> getConfigHistory(TicketQueryRequest req) {
        return ApiResult.success(configService.getConfigHistory(req));
    }
} 