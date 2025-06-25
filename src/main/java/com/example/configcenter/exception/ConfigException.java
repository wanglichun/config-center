package com.example.configcenter.exception;

/**
 * 配置异常
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public class ConfigException extends RuntimeException {
    
    public ConfigException(String message) {
        super(message);
    }
    
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ConfigException(Throwable cause) {
        super(cause);
    }
} 