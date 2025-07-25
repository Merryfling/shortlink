package dev.chanler.shortlink.admin.common.constant;

/**
 * 后管系统 Redis 缓存常量类
 * @author: Chanler
 */
public class RedisCacheConstant {
    /**
     * 用户注册分布式锁
     * 格式：short-link:lock:user-register:{username}
     */
    public static final String LOCK_USER_REGISTER_KEY = "short-link:lock:user-register:";

    /**
     * 用户登录缓存标识
     * 格式：short-link:login:{token}
     */
    public static final String USER_LOGIN_KEY = "short-link:login:";

    /**
     * 分组创建分布式锁
     * 格式：short-link:lock:group-create:{username}
     */
    public static final String LOCK_GROUP_CREATE_KEY = "short-link:lock:group-create:%s";
}
