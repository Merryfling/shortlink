package dev.chanler.shortlink.project.common.constant;

/**
 * Redis Key 常量类
 * @author: Chanler
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转前缀 key
     * 格式：short-link:goto:{fullShortUrl}
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link:goto:%s";

    /**
     * 短链接空值跳转锁前缀 key
     * 格式：short-link:is-null:goto_{fullShortUrl}
     */
    public static final String GOTO_IS_NULL_SHORT_LINK_KEY = "short-link:is-null:goto_%s";

    /**
     * 短链接跳转锁前缀 key
     * 格式：short-link:lock:goto:{fullShortUrl}
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link:lock:goto:%s";

    /**
     * 短链接修改分组 ID 锁前缀 Key
     * 格式：short-link:lock:update-gid:{fullShortUrl}
     */
    public static final String LOCK_GID_UPDATE_KEY = "short-link:lock:update-gid:%s";
}
