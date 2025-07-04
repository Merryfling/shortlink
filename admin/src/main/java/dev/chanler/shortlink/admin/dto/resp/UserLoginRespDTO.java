package dev.chanler.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 用户登录接口返回响应
 * @author: Chanler
 * @date: 2025/6/22 - 02:50
 */
@Data
@AllArgsConstructor
public class UserLoginRespDTO {
    /**
     * 用户 token
     */
    private String token;
}
