package jjun.server.websocket.service.chat;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import jjun.server.websocket.dto.request.ChatPagingDto;
import jjun.server.websocket.dto.response.ChatPagingResponseDto;
import jjun.server.websocket.entity.ChatRoom;
import jjun.server.websocket.repository.ChatRepository;
import jjun.server.websocket.repository.ChatRoomRepository;
import jjun.server.websocket.utils.ChatUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static jjun.server.websocket.repository.ChatRoomRepository.CHAT_SORTED_SET_;

/**
 * Redis 에 채팅 데이터를 넣어주는 클래스
 * -> Redis 의 Sorted set 활용 / Sort 방식 생성일지 -> Double 로 바꾼 후 사용
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRedisCacheService {

    public static final String NEW_CHAT = "NEW_CHAT";
    public static final String USERNAME_NICKNAME = "USERNAME_NICKNAME";

    private final ChatUtils chatUtils;
    private final ChatRepository chatRepository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, ChatMessageSaveDto> chatRedisTemplate;
    private final RedisTemplate<String, String> roomRedisTemplate;

    private ZSetOperations<String, ChatMessageSaveDto> zSetOperations;

    @PostConstruct
    private void init() {
        zSetOperations = chatRedisTemplate.opsForZSet();
    }

    /**
     * Redis Chat Data 삽입
     */
    public void addChat(ChatMessageSaveDto chatMessageSaveDto) {
        ChatMessageSaveDto savedData = ChatMessageSaveDto.createChatMessageSaveDto(chatMessageSaveDto);

        redisTemplate.opsForZSet().add(NEW_CHAT, savedData, chatUtils.changeLocalDateTimeToDouble(savedData.getCreatedAt()));
        redisTemplate.opsForZSet().add(CHAT_SORTED_SET_ + savedData.getRoomId(), savedData, chatUtils.changeLocalDateTimeToDouble(savedData.getCreatedAt()));
    }

    /**
     * Redis Chat Data 조회
     * // TODO BaseResponse 반환하도록!
     */
    public List<ChatPagingResponseDto> getChatsFromRedis(Long houseId, ChatPagingDto chatPagingDto) {

        // 마지막 채팅을 기준으로 Redis 의 Sorted set 에서 몇 번째 항목인지 파악
        ChatMessageSaveDto cursorDto = ChatMessageSaveDto.builder()
                .type(ChatMessageSaveDto.MessageType.TALK)
                .roomId(houseId.toString())
                .createdAt(chatPagingDto.getCursor())
                .message(chatPagingDto.getMessage())
                .sender(chatPagingDto.getSender())
                .build();

        // 마지막 Chat Data 의 cursor rank 조회
        Long rank = zSetOperations.reverseRank(CHAT_SORTED_SET_ + houseId, cursorDto);

        // Cursor 가 없는 경우 -> 최신 채팅 조회
        if (rank == null) {
            rank = 0L;
        } else {
            rank += 1;
        }

        // Redis 로부터 Chat Data 조회
        Set<ChatMessageSaveDto> chatMessageSaveDtos = zSetOperations.reverseRange(CHAT_SORTED_SET_ + houseId, rank, rank+10);

        List<ChatPagingResponseDto> chatMessageDtoList = chatMessageSaveDtos.stream()
                .map(ChatPagingResponseDto::byChatMessageDto)
                .collect(Collectors.toList());

        // Chat Data 가 하나의 페이지에서 보여줄 10개보다 부ㄷ족한 경우 -> MySQL 추가 조회
        if (chatMessageDtoList.size() != 10) {
            findOtherChatDataInMysql(chatMessageDtoList, houseId, chatPagingDto.getCursor());
        }

        // Redis Caching 닉네임으로 작성자 삽입
        // TODO 당시의 대표 프로필 닉네임 가져와서 지정
        for (ChatPagingResponseDto chatPagingResponseDto : chatMessageDtoList) {
            chatPagingResponseDto.setNickname(findUserNicknameByUsername(chatPagingResponseDto.getSender()));
        }

        return chatMessageDtoList;

    }

    public void cachingDBDataToRedis(ChatRoom chatRoom) {
        ChatMessageSaveDto chatMessageSaveDto = ChatMessageSaveDto.of(chatRoom);
        redisTemplate.opsForZSet().add(
                CHAT_SORTED_SET_ + chatMessageSaveDto.getRoomId(),
                chatMessageSaveDto,
                chatUtils.changeLocalDateTimeToDouble(chatMessageSaveDto.getCreatedAt())
        );
    }

    /**
     * Redis 회원 대표 닉네임 조회
     */
    public String findUserNicknameByUsername(String username) {
        String nickname = (String) roomRedisTemplate.opsForHash().get(USERNAME_NICKNAME, username);

        if (nickname != null) {
            return nickname;
        }

        // Redis 닉네임이 존재하지 않는다면 DB(MySQL) 에서 데이터 불러오기
//        User user = userRepository.findByUsername(username).orElse(null);

        // Insert
        roomRedisTemplate.opsForHash().put(USERNAME_NICKNAME, username, "임시 닉네임");

        return "임시 닉네임";
    }

    /**
     * 프로필 닉네임 수정 시, 변경 사항 Redis 캐시에도 저장
     */
    public void changeUserCachingNickname(String username, String changeNickname) {
        roomRedisTemplate.opsForHash().put(USERNAME_NICKNAME, username, changeNickname);
    }

    /**
     * 프로필 삭제 or 회원 탈퇴 시, 변경 사항 Redis 캐시에도 반영
     */
    public void deleteUserCachingNickname(String username) {
        roomRedisTemplate.opsForHash().delete(USERNAME_NICKNAME, username);
    }

    private void findOtherChatDataInMysql(List<ChatPagingResponseDto> chatMessageDtoList, Long houseId, String cursor) {
        String lastCursor;
        int dtoListSize = chatMessageDtoList.size();

        if (dtoListSize == 0 && cursor == null) {     // 데이터가 하나도 없는 경우, 현재 시간을 Cursor 로 설정
            lastCursor = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS"));
        } else if (dtoListSize == 0 && cursor != null) {   // Redis 에 적재된 마지막 데이터를 입력했을 경우
            lastCursor = cursor;
        } else {   // 데이터가 존재할 경우 CreatedAt = cursor
            lastCursor = chatMessageDtoList.get(chatMessageDtoList.size()-1).getCreatedAt();
        }

        Slice<ChatRoom> chatRoomSlice = chatRepository.findAllByCreatedAtBeforeAndHouse_IdOrderByCreatedDesc(
                lastCursor, houseId, PageRequest.of(0, 30)
        );

        for (ChatRoom chatRoom : chatRoomSlice.getContent()) {
            cachingDBDataToRedis(chatRoom);
        }

        // 추가 데이터가 없다면 그냥 리턴
        if (chatRoomSlice.getContent().isEmpty()) {
            return;
        }

        // 추가 데이터 존재 시, responseDto 에 데이터 추가
        for (int i=dtoListSize; i<=10; i++) {
            try {
                ChatRoom chatRoom = chatRoomSlice.getContent().get(i-dtoListSize);
                chatMessageDtoList.add(ChatPagingResponseDto.of(chatRoom));
            } catch (IndexOutOfBoundsException e) {
                log.error("Index 가 올바르지 않습니다.");
                return;
            }
        }

    }

}
