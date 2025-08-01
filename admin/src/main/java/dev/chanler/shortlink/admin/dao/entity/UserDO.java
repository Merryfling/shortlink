package dev.chanler.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import dev.chanler.shortlink.admin.common.database.BaseDO;
import lombok.Data;

/**
 * 用户持久层实体
 * @author: Chanler
 * @date: 2025/5/11 - 19:49
 */
@Data
@TableName("t_user")
public class UserDO extends BaseDO {
    /**
     * ID
     */
    private Long id;

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

    /**
     * 注销时间戳
     */
    private Long deletionTime;
}
