package com.kin.family.exception;

import lombok.Getter;

/**
 * 业务异常
 *
 * @author candong
 */
@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
