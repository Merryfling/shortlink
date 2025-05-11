package dev.chanler.shortlink.admin.common.convention.errorcode;

/**
 * 平台错误码
 * @author: Chanler
 * @date: 2025/5/11 - 20:28
 */
public interface IErrorCode {

    /**
     * 错误码
     */
    String code();

    /**
     * 错误信息
     */
    String message();
}
