package jjun.server.websocket.service;

import jjun.server.websocket.dto.ChatMessage;
import jjun.server.websocket.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;


/**
 * 채팅 메시지 발송을 일원화하기 위한 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate redisTemplate;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * 채팅방 조회
     * - destination 정보에서 roomId 추출
     */
    public String getRoomId(String destination) {
        int lastIdx = destination.lastIndexOf('/');
        if (lastIdx != -1) {
            return destination.substring(lastIdx + 1);
        } else {
            return "";
        }
    }

    /*
     * 채팅방 생성 - Random UUID 를 ID 로 가지는 채팅방 객체 생성 -> chatRooms 에 추가
     * **ID는 식별자로, 조회 시 사용
     */

    /*public ChatRoom createRoom(String name) {
        String randomId = UUID.randomUUID().toString();
        ChatRoom chatRoom = ChatRoom.builder()
                .roomId(randomId)
                .name(name)
                .build();
        chatRooms.put(randomId, chatRoom);   // 채팅방 리스트에 새로 개설한 채팅방 추가
        return chatRoom;
    }*/

    /*
     * 메시지 발송 - 지정한 웹 소켓 세션으로 메시지 발송
     * TALK 상태일 때 실행
     */

    public <T> void sendChatMessage(ChatMessage message) {
        message.setUserCount(chatRoomRepository.getUserCount(message.getRoomId()));

        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            log.info(message.getSender() + "님이 방에 입장했습니다");
            message.setMessage(message.getSender() + "님이 방에 입장했습니다");
            message.setSender("[알림]");
        } else if (ChatMessage.MessageType.QUIT.equals(message.getType())) {
            log.info(message.getSender() + "님이 방에서 나갔습니다.");
            message.setMessage(message.getSender() + "님이 방에서 나갔습니다.");
            message.setSender("[알림]");
        }
    }
}

