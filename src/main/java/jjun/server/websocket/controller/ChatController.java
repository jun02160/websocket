package jjun.server.websocket.controller;

import jjun.server.websocket.dto.ChatMessage;
import jjun.server.websocket.jwt.TokenProvider;
import jjun.server.websocket.repository.ChatRoomRepository;

import jjun.server.websocket.service.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;


@Slf4j
@RequiredArgsConstructor
@Controller
//@Controller
public class ChatController {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenProvider tokenProvider;
    private final ChannelTopic channelTopic;

    /**
     * WebSocket "/pub/chat/message" 로 들어오는 메시징 처리
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header("token") String token) {

        String nickname = tokenProvider.getUserNameFromJwt(token);
        // 로그인 회원 정보로 대화명 설정
        message.setSender(nickname);

        // 채팅방 입장 시, 대화명과 메시지 자동 세팅
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setSender("[알림]");
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
            log.info(nickname + "님이 입장하셨습니다.");
        }

        log.info("Topic 가져오기: {}", channelTopic.getTopic());
        // WebSocket 에서 발행된 메시지 -> Redis로 발행(publish)
        redisTemplate.convertAndSend(channelTopic.getTopic(), message);
    }
}
