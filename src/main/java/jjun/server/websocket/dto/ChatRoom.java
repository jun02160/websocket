package jjun.server.websocket.dto;

import jjun.server.websocket.service.ChatService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 채팅방 DTO
 */
@Slf4j
@Builder
@Getter
@Setter
public class ChatRoom {

    private String roomId;
    private String name;   // 이야기 집(House)과 연결

    public static ChatRoom create(String name) {
        return ChatRoom.builder()
                .roomId(UUID.randomUUID().toString())
                .name(name)
                .build();
    }
}

