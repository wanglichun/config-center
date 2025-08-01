package com.example.configcenter.controller;

import com.example.configcenter.common.ApiResult;
import com.example.configcenter.dto.MachineRegisterRequest;
import com.example.configcenter.service.MachineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * 机器配置订阅控制器
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/machine-config")
@CrossOrigin(origins = "*")
public class MachineController {

    @Autowired
    private MachineService machineService;

    /**
     * 注册机器实例 - 支持JSON请求体
     */
    @PostMapping("/register")
    public ApiResult<Boolean> registerMachine(@Valid @RequestBody MachineRegisterRequest request) {
        try {
            log.info("注册机器实例: groupName={}, instanceId={}, instanceIp={}, configKeys={}", 
                    request.getGroupName(), request.getInstanceId(), request.getInstanceIp(), request.getConfigKeys());
            
            boolean result = machineService.registerMachine(
                request.getGroupName(), 
                request.getInstanceId(), 
                request.getInstanceIp(), 
                request.getConfigKeys()
            );
            
            return result ? ApiResult.success(true) : ApiResult.error("注册机器实例失败");
        } catch (Exception e) {
            log.error("注册机器实例失败: groupName={}, instanceId={}", 
                     request.getGroupName(), request.getInstanceId(), e);
            return ApiResult.error("注册失败：" + e.getMessage());
        }
    }
}