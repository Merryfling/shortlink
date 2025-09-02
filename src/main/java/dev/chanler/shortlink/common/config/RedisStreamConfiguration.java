package dev.chanler.shortlink.common.config;

import dev.chanler.shortlink.mq.consumer.LinkStatsSaveConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.chanler.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_GROUP_KEY;
import static dev.chanler.shortlink.common.constant.RedisKeyConstant.SHORT_LINK_STATS_STREAM_TOPIC_KEY;

/**
 * Redis Stream 消息队列配置
 * @author: Chanler
 */
@Configuration
@RequiredArgsConstructor
public class RedisStreamConfiguration {

    private final RedisConnectionFactory redisConnectionFactory;
    private final LinkStatsSaveConsumer linkStatsSaveConsumer;

    @Bean
    public ExecutorService asyncStreamConsumer() {
        AtomicInteger index = new AtomicInteger();
        return new ThreadPoolExecutor(1,
                1,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("stream_consumer_short-link_stats_" + index.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer(
            ExecutorService asyncStreamConsumer) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions
                        .builder()
                        .batchSize(10)
                        .executor(asyncStreamConsumer)
                        .pollTimeout(Duration.ofSeconds(3))
                        .build();
        return StreamMessageListenerContainer.create(redisConnectionFactory, options);
    }

    @Bean(destroyMethod = "cancel")
    public Subscription shortLinkStatsSaveConsumerSubscription(
            StreamMessageListenerContainer<String, MapRecord<String, String, String>> streamMessageListenerContainer) {
        StreamMessageListenerContainer.StreamReadRequest<String> streamReadRequest =
                StreamMessageListenerContainer.StreamReadRequest.builder(
                                StreamOffset.create(SHORT_LINK_STATS_STREAM_TOPIC_KEY, ReadOffset.lastConsumed()))
                        .cancelOnError(throwable -> false)
                        .consumer(Consumer.from(SHORT_LINK_STATS_STREAM_GROUP_KEY, "stats-consumer"))
                        .autoAcknowledge(true)
                        .build();
        return streamMessageListenerContainer.register(streamReadRequest, linkStatsSaveConsumer);
    }
}
