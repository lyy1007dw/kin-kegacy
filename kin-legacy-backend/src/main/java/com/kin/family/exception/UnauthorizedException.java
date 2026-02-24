package com.kin.family.exception;

/**
 * 未授权异常
 *
 * @author candong
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super(401, message);
    }
}
