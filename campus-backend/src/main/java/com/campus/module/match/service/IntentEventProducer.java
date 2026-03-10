package com.campus.module.match.service;

import com.campus.module.match.config.IntentConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图事件生产者
 * 将用户行为事件通过 Redis Streams XADD 发布到消息队列
 * 使用 StringRedisTemplate 确保与 Consumer 端序列化一致
 */
@Slf4j
@Service
public class IntentEventProducer {

    private final StringRedisTemplate stringRedisTemplate;
    private final IntentConfig intentConfig;

    public IntentEventProducer(StringRedisTemplate stringRedisTemplate, IntentConfig intentConfig) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.intentConfig = intentConfig;
    }

    /**
     * 发布用户行为事件到 Redis Stream
     *
     * @param parentId   家长ID
     * @param tutorId    教员ID
     * @param actionType 行为类型
     */
    public void publishAction(Long parentId, Long tutorId, int actionType) {
        if (!intentConfig.isEnabled()) {
            return;
        }

        try {
            Map<String, String> eventData = new HashMap<>();
            eventData.put("parentId", String.valueOf(parentId));
            eventData.put("tutorId", String.valueOf(tutorId));
            eventData.put("actionType", String.valueOf(actionType));
            eventData.put("timestamp", String.valueOf(System.currentTimeMillis()));

            StringRecord record = StreamRecords.string(eventData)
                    .withStreamKey(intentConfig.getStreamKey());

            RecordId recordId = stringRedisTemplate.opsForStream().add(record);

            log.debug("意图事件已发布到Stream: key={}, recordId={}, parentId={}, tutorId={}, action={}",
                    intentConfig.getStreamKey(), recordId, parentId, tutorId, actionType);

        } catch (Exception e) {
            // 发布失败不影响主流程
            log.warn("意图事件发布失败(Redis不可用)，跳过: {}", e.getMessage());
        }
    }
}
