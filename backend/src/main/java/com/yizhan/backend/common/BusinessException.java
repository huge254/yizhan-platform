package com.yizhan.backend.common;

import lombok.Getter;

/**
 * 业务异常：业务校验失败时抛出，由全局异常处理器统一转成 Result。
 */
@Getter
public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
