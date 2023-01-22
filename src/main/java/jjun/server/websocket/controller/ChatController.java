package jjun.server.websocket.controller;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import jjun.server.websocket.jwt.TokenProvider;

import jjun.server.websocket.repository.ChatRoomRepository;
import jjun.server.websocket.service.chat.ChatRedisCacheService;
import jjun.server.websocket.service.chat.ChatService;
import jjun.server.websocket.service.redis.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/**
 * 서버에서 보내는 입장, 퇴장 안내 메시지 이외의 대화 메시지를 처리하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Controller
//@Controller
public class ChatController {

    private final TokenProvider tokenProvider;
    private final RedisPublisher redisPublisher;
    private final ChatRedisCacheService chatRedisCacheService;
    private final ChannelTopic topic;

    /**
     * WebSocket "/pub/chat/message" 로 들어오는 메시징 처리
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessageSaveDto message, @Header("token") String token) {

        String nickname = tokenProvider.getUserNameFromJwt(token);
        message.setNickname(nickname);
        message.setSender(nickname);         // 로그인 회원 정보로 대화명 설정
        message.setType(ChatMessageSaveDto.MessageType.TALK);
        message.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")));

        // WebSocket 에 발행된 메시지를 Redis 로 발행(publish)
        redisPublisher.publish(topic, message);
        chatRedisCacheService.addChat(message);

    }
}
