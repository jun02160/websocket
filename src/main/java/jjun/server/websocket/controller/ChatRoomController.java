package jjun.server.websocket.controller;

import jjun.server.websocket.dto.ChatRoom;
import jjun.server.websocket.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatRoomRepository chatRoomRepository;

    @GetMapping("/room")
    public String rooms(Model model) {
        log.info("Controller- /room 실행");
        return "/chat/room";
    }

    /**
     * 모든 채팅방 리스트 조회
     */
    @GetMapping("/rooms")
    @ResponseBody
    public List<ChatRoom> room() {
        log.info("# All Chat Rooms");
        return chatRoomRepository.findAllRoom();
    }

    /**
     * 채팅방 개설
     */
    @PostMapping("/room")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam String name) {
        log.info("# Create Chat Room, name: {}", name);
        return chatRoomRepository.createChatRoom(name);
    }

    /**
     * 채팅방 입장 화면
     */
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(Model model, @PathVariable String roomId) {
        model.addAttribute("roomId", roomId);
        return "/chat/roomdetail";
    }

    /**
     * 툭정 채팅방 조회
     */
    @GetMapping("/room/{roomId}")
    @ResponseBody
    public ChatRoom roomInfo(@PathVariable String roomId) {
        log.info("# Get Chat Room, roomId: {}", roomId);
        return chatRoomRepository.findRoomById(roomId);
    }
}