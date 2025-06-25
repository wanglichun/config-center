package com.example.configcenter.exception;

/**
 * 系统异常
 *
 * @author ConfigCenter Team
 * @version 1.0.0
 */
public class SystemException extends RuntimeException {
    
    public SystemException(String message) {
        super(message);
    }
    
    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SystemException(Throwable cause) {
        super(cause);
    }
}