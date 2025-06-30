package com.example.configcenter.controller;


import com.example.configcenter.common.ApiResult;
import com.example.configcenter.enums.ConfigStatusEnum;
import com.example.configcenter.enums.EnvironmentEnum;
import com.example.configcenter.utils.EnumUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/enum")
@CrossOrigin(origins = "*")
public class EnumController {

    @GetMapping("get_all_enum")
    public ApiResult<Map<String, Map<?, ?>>> getAllEnum() {
        Map<String, Map<?, ?>> enumMap = EnumUtils.getAllEnumMaps(
                EnvironmentEnum.class,
                ConfigStatusEnum.class
        );
        return ApiResult.success(enumMap);
    }
}
