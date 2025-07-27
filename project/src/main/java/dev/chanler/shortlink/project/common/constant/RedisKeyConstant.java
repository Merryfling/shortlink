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

    /**
     * 短链接统计判断是否新用户缓存标识
     * 格式：short-link:stats:uv:{fullShortUrl}
     */
    public static final String SHORT_LINK_STATS_UV_KEY = "short-link:stats:uv:%s";

    /**
     * 短链接统计判断是否新 IP 缓存标识
     * 格式：short-link:stats:uip:{fullShortUrl}
     */
    public static final String SHORT_LINK_STATS_UIP_KEY = "short-link:stats:uip:%s";

    /**
     * 短链接监控消息保存队列 Topic 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_TOPIC_KEY = "short-link:stats-stream";

    /**
     * 短链接监控消息保存队列 Group 缓存标识
     */
    public static final String SHORT_LINK_STATS_STREAM_GROUP_KEY = "short-link:stats-stream:only-group";
}
