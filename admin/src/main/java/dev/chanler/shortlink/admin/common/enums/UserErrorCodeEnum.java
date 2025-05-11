package dev.chanler.shortlink.admin.common.enums;

import dev.chanler.shortlink.admin.common.convention.errorcode.IErrorCode;

/**
 * 用户相关错误码
 * @author: Chanler
 */
public enum UserErrorCodeEnum implements IErrorCode {

    USER_NULL("B000200", "用户记录不存在"),
    USER_EXIST("B00201", "用户记录已存在");


    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
