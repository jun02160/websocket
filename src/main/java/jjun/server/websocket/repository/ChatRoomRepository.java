package jjun.server.websocket.repository;

import jjun.server.websocket.dto.ChatRoom;
import jjun.server.websocket.service.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {

    // Redis
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    private final RedisTemplate<String, Object> redisTemplate;
    private HashOperations<String, String, ChatRoom> opsHasChatRoom;

    @PostConstruct
    private void init() {
        opsHasChatRoom = redisTemplate.opsForHash();
    }

    public List<ChatRoom> findAllRoom() {
        return opsHasChatRoom.values(CHAT_ROOMS);
    }

    public ChatRoom findRoomById(String id) {
        return opsHasChatRoom.get(CHAT_ROOMS, id);
    }

    /**
     * 채팅방 생성 : 서버 간 채팅방 공유를 위해 Redis hash 에 저장한다.
     */
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        opsHasChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

}
