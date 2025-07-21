package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.common.PageResult;
import com.example.configcenter.context.ContextManager;
import com.example.configcenter.dto.ConfigItemDto;
import com.example.configcenter.dto.ConfigQueryDto;
import com.example.configcenter.entity.ConfigItem;
import com.example.configcenter.entity.ConfigHistory;
import com.example.configcenter.service.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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

    /**
     * 创建配置
     */
    @PostMapping()
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> createConfig(@RequestBody ConfigItem configItem) {
        try {
            boolean result = configService.createConfig(configItem);
            return result ? ApiResult.success(true) : ApiResult.error("编辑配置失败");
        } catch (Exception e) {
            return ApiResult.error("创建失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个配置
     */
    @GetMapping
    public ApiResult<ConfigItem> getConfig(@Valid ConfigQueryDto queryDto) {
        ConfigItem config = configService.getConfig(
            queryDto.getGroupName(), 
            queryDto.getConfigKey()
        );
        return ApiResult.success(config);
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
    public ApiResult<Boolean> editConfig(@PathVariable Long id, 
                                        @RequestBody ConfigItem configItem,
                                        HttpServletRequest request) {
        try {
            configItem.setUpdateBy(ContextManager.getContext().getUserEmail());
            boolean result = configService.updateConfig(configItem);
            return result ? ApiResult.success(true) : ApiResult.error("编辑配置失败");
        } catch (Exception e) {
            return ApiResult.error("编辑失败：" + e.getMessage());
        }
    }

    /**
     * 发布配置
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> publishConfig(@PathVariable Long id, HttpServletRequest request) {
        boolean result = configService.publishConfig(id);
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
     * 获取配置历史
     */
    @GetMapping("/{id}/history")
    public ApiResult<List<ConfigHistory>> getConfigHistory(@PathVariable Long id) {
        List<ConfigHistory> history = configService.getConfigHistory(id);
        return ApiResult.success(history);
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
} 