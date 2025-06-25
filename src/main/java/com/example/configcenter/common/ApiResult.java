package com.example.configcenter.common;

import lombok.Data;

/**
 * 统一API响应结果
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
@Data
public class ApiResult<T> {
    
    /**
     * 响应代码
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private long timestamp;
    
    /**
     * 是否成功
     */
    private boolean success;

    public ApiResult() {
        this.timestamp = System.currentTimeMillis();
    }

    public ApiResult(int code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = code == 200;
    }

    /**
     * 成功响应
     */
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "操作成功", data);
    }

    /**
     * 成功响应
     */
    public static <T> ApiResult<T> success(String message, T data) {
        return new ApiResult<>(200, message, data);
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "操作成功", null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResult<T> error(String message) {
        return new ApiResult<>(500, message, null);
    }

    /**
     * 错误响应
     */
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }

    /**
     * 参数错误
     */
    public static <T> ApiResult<T> badRequest(String message) {
        return new ApiResult<>(400, message, null);
    }

    /**
     * 未授权
     */
    public static <T> ApiResult<T> unauthorized(String message) {
        return new ApiResult<>(401, message, null);
    }

    /**
     * 禁止访问
     */
    public static <T> ApiResult<T> forbidden(String message) {
        return new ApiResult<>(403, message, null);
    }

    /**
     * 资源不存在
     */
    public static <T> ApiResult<T> notFound(String message) {
        return new ApiResult<>(404, message, null);
    }
} 