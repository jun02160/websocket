package jjun.server.websocket.utils.scheduler;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * Redis 에 채팅 데이터를 넣어주는 클래스
 * -> Redis 의 Sorted set 활용 / Sort 방식 생성일지 -> Doule 로 바꾼 후 사용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatRedisCacheService {

    private final RedisTemplate<String, ChatMessageSaveDto> chatRedisTemplate;
    private final RedisTemplate<String, String> roomRedisTemplate;

    // Redis 의 Chatting Data Caching 처리
    public void addChat(ChatMessageSaveDto chatMessageSaveDto) {
        ChatMessageSaveDto savedData = ChatMessageSaveDto.createChatMessageSaveDto(chatMessageSaveDto);

        redis
    }
}
