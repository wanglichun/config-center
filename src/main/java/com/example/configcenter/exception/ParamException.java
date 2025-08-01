package com.example.configcenter.exception;

import com.example.configcenter.constant.ExceptionConstant;

public class ParamException extends RuntimeException {
    private int code;
    private String message;

    public ParamException() {
        super();
    }

    public ParamException(String message) {
        super(message);
        this.code = ExceptionConstant.PARAM_EXCEPTION_CODE;
        this.message = message;
    }

}
