package jjun.server.websocket.repository;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import jjun.server.websocket.entity.ChatRoom;
import jjun.server.websocket.service.chat.ChatRedisCacheService;
import jjun.server.websocket.utils.ChatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * 채팅방에 관련된 데이터를 처리하는 클래스
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class ChatRoomRepository {

    // Redis CacheKeys
    public static final String CHAT_ROOMS = "CHAT_ROOM";
    public static final String CHAT_ROOM_ID_ = "CHAT_ROOM_ID_";
    public static final String SESSION_ID = "SESSION_ID";
    public static final String CHAT_SORTED_SET_ = "CHAT_SORTED_SET_";
//    public static final String USER_COUNT = "USER_COUNT";
//    public static final String ENTER_INFO = "ENTER_INFO";

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, String> roomRedisTemplate;
    private final RedisTemplate<String, ChatMessageSaveDto> chatRedisTemplate;

    private final HouseRepository houseRepository;
    private final ChatRedisCacheService chatRedisCacheService;
    private final ChatRepository chatRepository;
    private final ChatUtils chatUtils;


    private HashOperations<String, String, String> hashOpsChatRoom;  // 입장(Enter)한 방 목록 저장
//    private HashOperations<String, String, String> hashOpsEnterRoom;  // TODO EnterInfo / ChatRoom 로 나눠서 구현?
    private BoundHashOperations<String, String, String> setOperations;

    @PostConstruct
    private void init() {
        hashOpsChatRoom = roomRedisTemplate.opsForHash();
    }

   /* *//**
     * 모든 채팅방 조회
     *//*
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    *//**
     * 특정 채팅방 조회
     *//*
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    *//**
     * 채팅방 생성 : 서버 간 채팅방 공유를 위해 Redis hash 에 저장한다.
     *//*
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }*/

    /**
     * 유저가 입장한 채팅방 정보(roomId)와 유저 세션 정보(sessionId) 매핑하여 저장
     */
    public void enterChatRoom(String roomId, String sessionId, String username) {
        hashOpsChatRoom.put(SESSION_ID, sessionId, roomId);   // 세션(key) - 세션 ID - 채팅방 ID
        hashOpsChatRoom.put(CHAT_ROOM_ID_ + roomId, sessionId, username);  // 채팅방(key) - 세션 ID - 유저 ID
    }

    /**
     * WebSocket Disconnect 시, WebSocket Session ID 를 통해서 Redis 에서 삭제
     */
    public String disconnectWebSocket(String sessionId) {
        String roomId = hashOpsChatRoom.get(SESSION_ID, sessionId);
        hashOpsChatRoom.delete(CHAT_ROOM_ID_ + roomId, sessionId);
        hashOpsChatRoom.delete(SESSION_ID, sessionId);
        return roomId;
    }

    /**
     * 채팅 Unsubscribe 시
     */
    public String leaveChatRoom(String sessionId) {
        String roomId = hashOpsChatRoom.get(SESSION_ID, sessionId);
        hashOpsChatRoom.delete(CHAT_ROOM_ID_ + roomId, sessionId);
        return roomId;
    }

    /**
     * 채팅 참여자 리스트 생성
     */
    public List<String> findUsersInHouse(String roomId, String sessionId) {
        setOperations = roomRedisTemplate.boundHashOps(CHAT_ROOM_ID_ + roomId);
        ScanOptions scanOptions = ScanOptions.scanOptions().build();
        List<String> userListInHouse = new ArrayList<>();

        try (Cursor<Map.Entry<String, String>> cursor = setOperations.scan(scanOptions)) {
            while (cursor.hasNext()) {
                Map.Entry<String, String> data = cursor.next();
                userListInHouse.add(chatRedisCacheService.findUserNicknameByUsername(data.getValue()));
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return userListInHouse;

    }

    /*public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    *//**
     * 유저 세션으로 입장한 상태인 채팅방 ID 조회 : setUserEnterInfo() 로 저장했던 것을 찾는 과정
     *//*
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    *//**
     * 유저 세션 정보와 매핑된 채팅방 정보 (sessionId + roomId) 삭제
     *//*
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }
*/
    /**
     * 채팅방 유저 수 조회
     */
    /*public long getUserCount(String roomId) {
        return Long.valueOf(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
    }*/
/*

    */
/**
     * 채팅방에 새로운 유저가 입장한 경우 => 인원 수 +1
     *//*

    public long plusUserCount(String roomId) {
        return Optional.ofNullable(
                valueOps.increment(USER_COUNT + "_" + roomId)
        ).orElse(0L);
    }

    */
/**
     * 채팅방에서 유저가 퇴장한 경우 => 인원 수 -1
     *//*

    public long minusUserCount(String roomId) {
        return Optional.ofNullable(
                valueOps.decrement(USER_COUNT + "_" + roomId)
        ).filter(count -> count > 0).orElse(0L);
    }
*/

}
