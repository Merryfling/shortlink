package dev.chanler.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author: Chanler
 * @date: 2025/6/22 - 02:29
 */
@Data
public class UserUpdateReqDTO {
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
