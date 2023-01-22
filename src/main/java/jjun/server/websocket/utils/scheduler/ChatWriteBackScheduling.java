package jjun.server.websocket.utils.scheduler;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import jjun.server.websocket.entity.House;
import jjun.server.websocket.repository.ChatJdbcRepository;
import jjun.server.websocket.repository.HouseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis 데이터 쓰기 캐싱 전략 : Write Back
 * - Data 캐시에 저장 -> 캐시에 있는 데이터 일정 기간동안 보관 -> 모여있는 데이터 DB에 저장 -> 캐시에 있는 데이터 삭제
 *
 * 스케줄링을 통해 1시간마다 Redis 에 있는 데이터를 List 로 바꾼 후,
 * Batch Insert 를 Query 를 통해 데이터 MySQL 에 삽입
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWriteBackScheduling {

    private final RedisTemplate<String, Object> chatJdbcRepository;
    private final RedisTemplate<String, ChatMessageSaveDto> chatRedisTemplate;

    private final ChatJdbcRepository chatJdbcRepository;
    private final HouseRepository houseRepository;

    // 1시간 마다 Chatting Data Redis -> MySQL 에 저장
    @Scheduled(cron = "0 0 0/1 * * *")
    @Transactional
    public void writeBack() {
        log.info("Scheduling start");

        BoundZSetOperations<String, ChatMessageSaveDto> setOperations = chatRedisTemplate.boundZSetOps("NEW_CHAT");
        ScanOptions scanOptions = ScanOptions.scanOptions().build();

        List<Chat> chatList = new ArrayList<>();
        try (Cursor<ZSetOperations.TypedTuple<ChatMessageSaveDto>> cursor = setOperations.scan(scanOptions)) {
            while (cursor.hasNext()) {
                ZSetOperations.TypedTuple<ChatMessageSaveDto> chatMessageDto = cursor.next();

                House house = houseRepository.findById(Long.parseLong(chatMessageDto.getValue().getRoomId()));

                if (workSpace == null) {
                    continue;
                }

                chatList.add(ChatRoom.of(chatMessageDto.getValue(), workSpace));
            }
            chatJdbcRepository.batchInsertRoomInventories(chatList);

        } catch (Exception e) {
            log.error(e.getMessage());
        }
        log.info("Scheduling Done");
    }

}
