package dev.chanler.shortlink.admin.common.convention.exception;

import dev.chanler.shortlink.admin.common.convention.errorcode.BaseErrorCode;
import dev.chanler.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * 客户端异常
 * @author: Chanler
 * @date: 2025/5/11 - 20:43
 */
public class ClientException extends AbstractException {

    public ClientException(IErrorCode errorCode) {
        this(null, null, errorCode);
    }

    public ClientException(String message) {
        this(message, null, BaseErrorCode.CLIENT_ERROR);
    }

    public ClientException(String message, IErrorCode errorCode) {
        this(message, null, errorCode);
    }

    public ClientException(String message, Throwable throwable, IErrorCode errorCode) {
        super(message, throwable, errorCode);
    }

    @Override
    public String toString() {
        return "ClientException{" +
                "code='" + errorCode + "'," +
                "message='" + errorMessage + "'" +
                '}';
    }
}