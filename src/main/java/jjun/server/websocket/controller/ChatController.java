package jjun.server.websocket.controller;

import jjun.server.websocket.dto.ChatMessage;
import jjun.server.websocket.dto.ChatRoomDto;
import jjun.server.websocket.service.ChatService;
import jjun.server.websocket.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RequiredArgsConstructor
@Controller
//@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
//    private final ChatService chatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessage message) {
        if (ChatMessage.MessageType.ENTER.equals(message.getType())) {
            message.setMessage(message.getSender() + "님이 입장하셨습니다.");
            log.info(message.getSender() + "님이 입장하셨습니다.");
        }
        messagingTemplate.convertAndSend("/sub/chat/room/" + message.getRoomId(), message);
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
