package dev.chanler.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户注册请求参数
 * @author: Chanler
 * @date: 2025/6/21 - 21:04
 */
@Data
public class UserRegisterReqDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;
}
