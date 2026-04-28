package com.campus.module.match.config;

import com.campus.module.match.service.IntentStreamConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;

import java.time.Duration;

/**
 * Redis Streams 意图事件流配置
 * 启动时自动创建消费者组，并注册 StreamMessageListenerContainer
 * 通过 campus.intent.enabled=true 控制是否启用
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "campus.intent", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IntentStreamConfig {

    private final IntentConfig intentConfig;
    private final StringRedisTemplate stringRedisTemplate;

    public IntentStreamConfig(IntentConfig intentConfig, StringRedisTemplate stringRedisTemplate) {
        this.intentConfig = intentConfig;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 创建并启动 StreamMessageListenerContainer
     * 自动从 Redis Stream 消费意图事件
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public StreamMessageListenerContainer<String, MapRecord<String, String, String>> intentStreamListenerContainer(
            RedisConnectionFactory connectionFactory,
            IntentStreamConsumer consumer) {

        // 确保消费者组存在
        createConsumerGroupIfAbsent();

        // 配置 ListenerContainer
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String, MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(intentConfig.getStreamPollTimeout()))
                        .build();

        StreamMessageListenerContainer<String, MapRecord<String, String, String>> container =
                StreamMessageListenerContainer.create(connectionFactory, options);

        // 注册消费者：使用消费者组模式，自动 ACK
        container.receiveAutoAck(
                Consumer.from(intentConfig.getConsumerGroup(), intentConfig.getConsumerName()),
                StreamOffset.create(intentConfig.getStreamKey(), ReadOffset.lastConsumed()),
                consumer
        );

        log.info("Redis Streams 意图消费者已注册: stream={}, group={}, consumer={}",
                intentConfig.getStreamKey(), intentConfig.getConsumerGroup(), intentConfig.getConsumerName());

        return container;
    }

    /**
     * 创建消费者组（如果不存在）
     * 需要 Stream 已存在，所以先尝试创建 Stream
     */
    private void createConsumerGroupIfAbsent() {
        try {
            // 尝试创建消费者组
            // 如果 Stream 不存在，先通过 MKSTREAM 创建
            stringRedisTemplate.opsForStream().createGroup(intentConfig.getStreamKey(), intentConfig.getConsumerGroup());
            log.info("创建Redis Stream消费者组: stream={}, group={}",
                    intentConfig.getStreamKey(), intentConfig.getConsumerGroup());
        } catch (Exception e) {
            // 消费者组已存在（BUSYGROUP），正常忽略
            if (e.getMessage() != null && e.getMessage().contains("BUSYGROUP")) {
                log.debug("消费者组已存在: {}", intentConfig.getConsumerGroup());
            } else {
                log.warn("创建消费者组时出错（可能Redis未连接），将在首次使用时重试: {}", e.getMessage());
            }
        }
    }
}
