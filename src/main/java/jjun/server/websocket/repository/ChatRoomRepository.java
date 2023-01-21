package jjun.server.websocket.repository;

import jjun.server.websocket.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
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
    private static final String CHAT_ROOMS = "CHAT_ROOM";
    public static final String USER_COUNT = "USER_COUNT";
    public static final String ENTER_INFO = "ENTER_INFO";

    @Resource(name = "redisTemplate")
    private HashOperations<String, String, ChatRoom> hashOpsChatRoom;
    @Resource(name = "redisTemplate")
    private HashOperations<String, String, String> hashOpsEnterInfo;
    @Resource(name = "redisTemplate")
    private ValueOperations<String, String> valueOps;

    /**
     * 모든 채팅방 조회
     */
    public List<ChatRoom> findAllRoom() {
        return hashOpsChatRoom.values(CHAT_ROOMS);
    }

    /**
     * 특정 채팅방 조회
     */
    public ChatRoom findRoomById(String id) {
        return hashOpsChatRoom.get(CHAT_ROOMS, id);
    }

    /**
     * 채팅방 생성 : 서버 간 채팅방 공유를 위해 Redis hash 에 저장한다.
     */
    public ChatRoom createChatRoom(String name) {
        ChatRoom chatRoom = ChatRoom.create(name);
        hashOpsChatRoom.put(CHAT_ROOMS, chatRoom.getRoomId(), chatRoom);
        return chatRoom;
    }

    /**
     * 유저가 입장한 채팅방 정보(roomId)와 유저 세션 정보(sessionId) 매핑하여 저장
     */
    public void setUserEnterInfo(String sessionId, String roomId) {
        hashOpsEnterInfo.put(ENTER_INFO, sessionId, roomId);
    }

    /**
     * 유저 세션으로 입장한 상태인 채팅방 ID 조회 : setUserEnterInfo() 로 저장했던 것을 찾는 과정
     */
    public String getUserEnterRoomId(String sessionId) {
        return hashOpsEnterInfo.get(ENTER_INFO, sessionId);
    }

    /**
     * 유저 세션 정보와 매핑된 채팅방 정보 (sessionId + roomId) 삭제
     */
    public void removeUserEnterInfo(String sessionId) {
        hashOpsEnterInfo.delete(ENTER_INFO, sessionId);
    }

    /**
     * 채팅방 유저 수 조회
     */
    public int getUserCount(String roomId) {
        return Integer.parseInt(Optional.ofNullable(valueOps.get(USER_COUNT + "_" + roomId)).orElse("0"));
    }

    /**
     * 채팅방에 새로운 유저가 입장한 경우 => 인원 수 +1
     * // TODO 인원 수 타입 long VS int
     */
    public int plusUserCount(String roomId) {
        return Integer.parseInt(String.valueOf(Optional.ofNullable(
                valueOps.increment(USER_COUNT + "_" + roomId)
        ).orElse(0L)));
    }

    /**
     * 채팅방에서 유저가 퇴장한 경우 => 인원 수 -1
     */
    public int minusUserCount(String roomId) {
        return Integer.parseInt(String.valueOf(Optional.ofNullable(
                valueOps.decrement(USER_COUNT + "_" + roomId)
        ).filter(count -> count > 0).orElse(0L)));
    }

}
