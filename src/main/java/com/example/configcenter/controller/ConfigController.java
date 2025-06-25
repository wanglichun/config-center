package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.common.PageResult;
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
     * 获取单个配置
     */
    @GetMapping
    public ApiResult<ConfigItem> getConfig(@Valid ConfigQueryDto queryDto) {
        ConfigItem config = configService.getConfig(
            queryDto.getAppName(), 
            queryDto.getEnvironment(), 
            queryDto.getGroupName(), 
            queryDto.getConfigKey()
        );
        return ApiResult.success(config);
    }

    /**
     * 获取应用的所有配置
     */
    @GetMapping("/list")
    public ApiResult<List<ConfigItem>> getConfigs(
            @RequestParam String appName,
            @RequestParam String environment) {
        List<ConfigItem> configs = configService.getConfigs(appName, environment);
        return ApiResult.success(configs);
    }

    /**
     * 获取配置组的配置键值对
     */
    @GetMapping("/map")
    public ApiResult<Map<String, String>> getConfigMap(
            @RequestParam String appName,
            @RequestParam String environment,
            @RequestParam String groupName) {
        Map<String, String> configMap = configService.getConfigMap(appName, environment, groupName);
        return ApiResult.success(configMap);
    }

    /**
     * 分页查询配置
     */
    @GetMapping("/page")
    public ApiResult<PageResult<ConfigItem>> getConfigPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String appName,
            @RequestParam(required = false) String environment,
            @RequestParam(required = false) String keyword) {
        // 这里需要实现分页查询逻辑
        List<ConfigItem> configs = configService.searchConfigs(keyword, appName, environment);
        PageResult<ConfigItem> pageResult = new PageResult<>(configs, configs.size(), pageNum, pageSize);
        return ApiResult.success(pageResult);
    }

    /**
     * 创建配置
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> createConfig(@Valid @RequestBody ConfigItemDto configDto, 
                                          HttpServletRequest request) {
        ConfigItem configItem = convertToEntity(configDto);
        configItem.setCreateBy(getCurrentUser(request));
        configItem.setUpdateBy(getCurrentUser(request));
        
        boolean result = configService.createConfig(configItem);
        return result ? ApiResult.success(true) : ApiResult.error("创建配置失败");
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> updateConfig(@PathVariable Long id, 
                                          @Valid @RequestBody ConfigItemDto configDto,
                                          HttpServletRequest request) {
        ConfigItem configItem = convertToEntity(configDto);
        configItem.setId(id);
        configItem.setUpdateBy(getCurrentUser(request));
        
        boolean result = configService.updateConfig(configItem);
        return result ? ApiResult.success(true) : ApiResult.error("更新配置失败");
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
     * 发布配置
     */
    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> publishConfig(@PathVariable Long id, HttpServletRequest request) {
        String publisher = getCurrentUser(request);
        boolean result = configService.publishConfig(id, publisher);
        return result ? ApiResult.success(true) : ApiResult.error("发布配置失败");
    }

    /**
     * 批量发布配置
     */
    @PostMapping("/batch-publish")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> publishConfigs(@RequestParam String appName,
                                            @RequestParam String environment,
                                            HttpServletRequest request) {
        String publisher = getCurrentUser(request);
        boolean result = configService.publishConfigs(appName, environment, publisher);
        return result ? ApiResult.success(true) : ApiResult.error("批量发布配置失败");
    }

    /**
     * 回滚配置
     */
    @PostMapping("/{id}/rollback")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Boolean> rollbackConfig(@PathVariable Long id,
                                            @RequestParam Long targetVersion,
                                            HttpServletRequest request) {
        String operator = getCurrentUser(request);
        boolean result = configService.rollbackConfig(id, targetVersion, operator);
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
     * 搜索配置
     */
    @GetMapping("/search")
    public ApiResult<List<ConfigItem>> searchConfigs(@RequestParam String keyword,
                                                     @RequestParam(required = false) String appName,
                                                     @RequestParam(required = false) String environment) {
        List<ConfigItem> configs = configService.searchConfigs(keyword, appName, environment);
        return ApiResult.success(configs);
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
     * 导入配置
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<Integer> importConfigs(@RequestBody List<ConfigItemDto> configDtos,
                                           HttpServletRequest request) {
        String operator = getCurrentUser(request);
        List<ConfigItem> configItems = configDtos.stream()
            .map(this::convertToEntity)
            .collect(java.util.stream.Collectors.toList());
        
        int successCount = configService.importConfigs(configItems, operator);
        return ApiResult.success(successCount);
    }

    /**
     * 导出配置
     */
    @GetMapping("/export")
    public ApiResult<List<ConfigItem>> exportConfigs(@RequestParam String appName,
                                                     @RequestParam String environment) {
        List<ConfigItem> configs = configService.exportConfigs(appName, environment);
        return ApiResult.success(configs);
    }

    /**
     * 加密配置值
     */
    @PostMapping("/encrypt")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<String> encryptConfig(@RequestParam String configValue) {
        String encrypted = configService.encryptConfig(configValue);
        return ApiResult.success(encrypted);
    }

    /**
     * 解密配置值
     */
    @PostMapping("/decrypt")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DEVELOPER')")
    public ApiResult<String> decryptConfig(@RequestParam String encryptedValue) {
        String decrypted = configService.decryptConfig(encryptedValue);
        return ApiResult.success(decrypted);
    }

    /**
     * 转换DTO到实体
     */
    private ConfigItem convertToEntity(ConfigItemDto dto) {
        ConfigItem entity = new ConfigItem();
        entity.setAppName(dto.getAppName());
        entity.setEnvironment(dto.getEnvironment());
        entity.setGroupName(dto.getGroupName());
        entity.setConfigKey(dto.getConfigKey());
        entity.setConfigValue(dto.getConfigValue());
        entity.setDataType(dto.getDataType());
        entity.setDescription(dto.getDescription());
        entity.setEncrypted(dto.getEncrypted());
        entity.setTags(dto.getTags());
        entity.setRemark(dto.getRemark());
        return entity;
    }

    /**
     * 获取当前用户
     */
    private String getCurrentUser(HttpServletRequest request) {
        // 从JWT Token或Session中获取当前用户
        // 这里简化处理，实际应该从SecurityContext获取
        return "admin"; // 临时返回
    }
} 