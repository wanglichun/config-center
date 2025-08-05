package com.example.configcenter.context;

import lombok.Data;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Map;

@RequestScope
@Data
public class Context {
    // 全局唯一标识
    private String traceId;
    // 当前操作标识
    private String spanId;
    // 父操作标识
    private String parentSpanId;
    // 用户标识
    private String userEmail;
    // 请求标识
    private String requestId;
    // 开始时间
    private long startTime;
    // 标签信息
    private Map<String, String> tags;


}
