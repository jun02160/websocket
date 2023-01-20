package jjun.server.websocket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jjun.server.websocket.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

/**
 * Redis 구독 서비스
 *
 * Redis에 메시지 발행이 될 때까지 대기했다가 메시지 발행되면 해당 메시지를 읽어 처리하는 리스너이다.
 * 여기서 Redis에 메시지가 발행되면 해당 메시지가 ChatMessage로 변환되고 messaging Template를 이용하여
 * 채팅방의 모든 WebSocket 클라이언트들에게 메시지를 전달할 수 있다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;

    /**
     * Redis 에서 pub 으로 메시지가 발행되면 대기하고 있던 onMessage 가 해당 메시지를 받아서 처리한다.
     */
    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            // redis 에서 발행된 데이터를 받아 역직렬화(deserialize, 스트림->객체 재구성)
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            // ChatMessage 객체로 매핑
            ChatMessage roomMessage = objectMapper.readValue(publishMessage, ChatMessage.class);
            // WebSocket 구독자에게 채팅 메시지 Send
            messagingTemplate.convertAndSend("/sub/chat/room/" + roomMessage.getRoomId(), roomMessage);
        } catch (Exception e) {
//            log.info(e.getMessage());
        }
    }
}