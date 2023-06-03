package com.marvel.communityforum.event;

import com.alibaba.fastjson.JSONObject;
import com.marvel.communityforum.entity.Event;
import com.marvel.communityforum.entity.Message;
import com.marvel.communityforum.service.MessageService;
import com.marvel.communityforum.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("Kafka message is null");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("Kafka message format is wrong");
            return;
        }

        //conversation between user and system
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getSubjectUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("subjectType", event.getSubjectType());
        content.put("subjectId", event.getSubjectId());
        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> en : event.getData().entrySet()) {
                content.put(en.getKey(), en.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
