package com.campus.module.match.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 意图事件消费者
 * 从 Redis Stream 中通过 XREADGROUP 消费用户行为事件
 * 异步调用 RealtimeIntentService 进行意图标签更新
 */
@Slf4j
@Component
public class IntentStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {

    private final RealtimeIntentService realtimeIntentService;

    public IntentStreamConsumer(RealtimeIntentService realtimeIntentService) {
        this.realtimeIntentService = realtimeIntentService;
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        try {
            Map<String, String> body = message.getValue();

            Long parentId = Long.parseLong(body.get("parentId"));
            Long tutorId = Long.parseLong(body.get("tutorId"));
            int actionType = Integer.parseInt(body.get("actionType"));

            log.debug("Stream消费意图事件: recordId={}, parentId={}, tutorId={}, action={}",
                    message.getId(), parentId, tutorId, actionType);

            // 委托给 RealtimeIntentService 完成 ZSET 意图标签更新
            realtimeIntentService.handleUserAction(parentId, tutorId, actionType);

            log.debug("意图事件处理完成: recordId={}", message.getId());

        } catch (Exception e) {
            log.warn("Stream意图事件处理异常: recordId={}, error={}", message.getId(), e.getMessage());
        }
    }
}
