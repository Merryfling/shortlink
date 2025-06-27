package dev.chanler.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户登录请求参数
 * @author: Chanler
 * @date: 2025/6/22 - 02:51
 */
@Data
public class UserLoginReqDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
