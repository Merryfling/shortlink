package dev.chanler.shortlink.initialize;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static dev.chanler.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

/**
 * 初始化短链接监控消息队列消费者组
 * @author: Chanler
 */
@Component
@RequiredArgsConstructor
public class LinkStatsStreamInitializeTask implements InitializingBean {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 无论 Stream Key 是否已存在，都尝试创建消费者组；
        // 已存在时 Redis 返回 BUSYGROUP，忽略即可；
        // ReadOffset.latest() 等价于 XGROUP CREATE key group $ MKSTREAM
        try {
            stringRedisTemplate.opsForStream()
                    .createGroup(SHORT_LINK_STATS_STREAM_TOPIC_KEY, ReadOffset.latest(), SHORT_LINK_STATS_STREAM_GROUP_KEY);
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("BUSYGROUP")) {
                // 组已存在，忽略
                return;
            }
            // 其他异常抛出，便于启动期暴露配置问题
            throw e;
        }
    }
}
