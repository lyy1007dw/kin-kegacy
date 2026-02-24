package com.kin.family.exception;

/**
 * 禁止访问异常
 *
 * @author candong
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super(403, message);
    }
}
