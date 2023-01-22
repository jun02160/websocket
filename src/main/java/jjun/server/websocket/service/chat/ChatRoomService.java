package jjun.server.websocket.service.chat;

import jjun.server.websocket.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    public void enterChatRoom(String roomId, String sessionId, String username) {
        chatRoomRepository.enterChatRoom(roomId, sessionId, username);
    }

    public String disconnectWebSocket(String sessionId) {
        return chatRoomRepository.disconnectWebSocket(sessionId);
    }

    public String leaveChatRoom(String sessionId) {
        return chatRoomRepository.leaveChatRoom(sessionId);
    }

    public List<String> findUser(String roomId, String sessionId) {
        return chatRoomRepository.findUsersInHouse(roomId, sessionId);
    }
}
