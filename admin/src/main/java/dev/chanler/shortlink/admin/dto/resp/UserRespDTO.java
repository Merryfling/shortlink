package dev.chanler.shortlink.admin.dto.resp;

import lombok.Data;

/**
 * 用户返回参数响应
 * @author: Chanler
 * @date: 2025/5/11 - 20:03
 */
@Data
public class UserRespDTO {
    /**
     * ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

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
