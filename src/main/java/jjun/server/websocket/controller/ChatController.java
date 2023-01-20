package jjun.server.websocket.controller;

import jjun.server.websocket.dto.ChatMessage;
import jjun.server.websocket.repository.ChatRoomRepository;

import jjun.server.websocket.service.RedisPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;


@Slf4j
@RequiredArgsConstructor
@Controller
//@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;
    private final ChatRoomRepository chatRoomRepository;

    /**
     * WebSocket "/pub/chat/message" 로 들어오는 메시징 처리
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
            log.info(message.getSender() + "님이 입장하셨습니다.");
        }
        // WebSocket 에서 발행된 메시지 -> Redis로 발행(publish)
        redisPublisher.publish(chatRoomRepository.getTopic(message.getRoomId()), message);
    }

    /*@RequestMapping(value = "/chat", method = POST)
    public ChatRoom createRoom(@RequestBody ChatRoomDto roomDto) {
        return chatService.createRoom(roomDto.getName());
    }

    @RequestMapping(value = "/chat", method = GET)
    public List<ChatRoom> findAllRoom() {
        return chatService.findAllRoom();
    }*/
}
