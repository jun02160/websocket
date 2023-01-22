package jjun.server.websocket.controller;

import jjun.server.websocket.dto.request.ChatPagingDto;
import jjun.server.websocket.dto.response.ChatPagingResponseDto;
import jjun.server.websocket.service.chat.ChatRedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
public class ChatDataController {

    private final ChatRedisCacheService chatRedisCacheService;

    @PostMapping("/api/chats/{houseId}")
    public List<ChatPagingResponseDto> getChatting(@PathVariable Long houseId, @RequestBody(required = false)ChatPagingDto chatPagingDto) {

        // Cursor 가 요청 값으로 넘어오지 않았다면, 현재 시간을 기준으로 페이징 처리
        if (chatPagingDto == null || chatPagingDto.getCursor() == null || chatPagingDto.getCursor().equals("")) {
            chatPagingDto = ChatPagingDto.builder()
                    .cursor(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS")))
                    .build();
        }

        return chatRedisCacheService.getChatsFromRedis(houseId, chatPagingDto);
    }
}
