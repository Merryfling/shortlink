package dev.chanler.shortlink.project.common.constant;

/**
 * Redis Key 常量类
 * @author: Chanler
 */
public class RedisKeyConstant {

    /**
     * 短链接跳转前缀 key
     * 格式：short-link_goto_{gid}
     */
    public static final String GOTO_SHORT_LINK_KEY = "short-link_goto_%s";

    /**
     * 短链接跳转锁前缀 key
     * 格式：short-link_goto_{gid}
     */
    public static final String LOCK_GOTO_SHORT_LINK_KEY = "short-link_lock_goto_%s";
}
