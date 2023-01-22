package jjun.server.websocket.utils.scheduler;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import jjun.server.websocket.repository.ChatRoomRepository;
import jjun.server.websocket.utils.ChatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static jjun.server.websocket.repository.ChatRoomRepository.CHAT_SORTED_SET_;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCachingInRedisScheduling {

    private final RedisTemplate<String, ChatMessageSaveDto> chatRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    private final ChatUtils chatUtils;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void chatCaching() {
        log.info("[Scheduling] Redis Chat Caching Start");

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(CHAT_SORTED_SET_ + "*")
                .build();

        Cursor<String> cursor = redisTemplate.scan(scanOptions);

        // 기존 Redis Caching 데이터 삭제
        while (cursor.hasNext()) {
            String matchedKey = cursor.next();
            log.info("Matched Key: {}", matchedKey);
            redisTemplate.delete(matchedKey);
        }

        // Redis Caching 데이터 일주일치 삭제하기
        // TODO 이야기 집의 경우, 데이터 삭제 주기 서비스 기능대로 하루로 설정할지?
        chatUtils.cachingDataToRedisFromDB();
    }
}
