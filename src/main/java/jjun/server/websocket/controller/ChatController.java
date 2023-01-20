package jjun.server.websocket.controller;

import jjun.server.websocket.dto.ChatRoomDto;
import jjun.server.websocket.service.ChatService;
import jjun.server.websocket.dto.ChatRoom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Slf4j
@RequiredArgsConstructor
@RestController
//@Controller
public class ChatController {

    private final ChatService chatService;

    @RequestMapping(value = "/chat", method = POST)
    public ChatRoom createRoom(@RequestBody ChatRoomDto roomDto) {
        return chatService.createRoom(roomDto.getName());
    }

    @RequestMapping(value = "/chat", method = GET)
    public List<ChatRoom> findAllRoom() {
        return chatService.findAllRoom();
    }
}
