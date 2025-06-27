package com.example.configcenter.controller;


import com.example.configcenter.common.ApiResult;
import com.example.configcenter.dto.LoginResponse;
import com.example.configcenter.enums.EnvironmentEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.PortResolverImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

    private final static HashMap<String, List<String>> enumMap = new HashMap<>();

    @GetMapping("get_all_enum")
    public ApiResult<Map<String, List<String>>> getAllEnum() {
        enumMap.put("", EnvironmentEnum.values().);
    }
}
