package jjun.server.websocket.dto;

import jjun.server.websocket.service.ChatService;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

/**
 * 채팅방 DTO
 */
@Slf4j
@Getter
@Setter
public class ChatRoom {

    private String roomId;
    private String name;   // 이야기 집(House)과 연결
    private Set<WebSocketSession> sessions = new HashSet<>();  // WebSocketSession : Spring 에서 WebSocket Connection 이 맺어진 세션

    @Builder
    public ChatRoom(String roomId, String name) {
        this.roomId = roomId;
        this.name = name;
    }

    public void handlerActions(WebSocketSession session, ChatMessage chatMessage, ChatService chatService) {
        if (chatMessage.getType().equals(ChatMessage.MessageType.ENTER)) {
            sessions.add(session);
            chatMessage.setMessage(chatMessage.getSender()+"님이 입장했습니다.");
            log.info(chatMessage.getSender()+"님이 입장했습니다.");
        }
        log.info(chatMessage+" 메시지가 정상적으로 전송되었습니다.");

        sendMessage(chatMessage, chatService);
    }

    private <T> void sendMessage(T message, ChatService chatService) {
        sessions.parallelStream()
                .forEach(session ->
                        chatService.sendMessage(session, message));
    }
}

