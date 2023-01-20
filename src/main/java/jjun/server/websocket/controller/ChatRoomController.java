package jjun.server.websocket.controller;

import jjun.server.websocket.dto.ChatRoom;
import jjun.server.websocket.dto.ChatRoomDto;
import jjun.server.websocket.repository.ChatRoomRepository;
import jjun.server.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/chat")
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * 모든 채팅방 리스트 조회
     */
    @GetMapping("/rooms")
    public List<ChatRoom> rooms() {
        log.info("# All Chat Rooms");
        return chatService.findAllRoom();
    }

    /**
     * 채팅방 개설
     */
    @PostMapping("/room")
    public ResponseEntity createRoom(@RequestParam ChatRoomDto roomDto) {
        log.info("# Create Chat Room, name: {}", roomDto.getName());
        try {
            ChatRoom chatRoom = chatService.createRoom(roomDto.getName());
            return ResponseEntity.ok(chatRoom);
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest();
        }
    }

    /**
     * 채팅방 입장 화면
     */
    @GetMapping("/room/enter/{roomId}")
    public String roomDetail(@PathVariable String roomId) {
        return "입장 완료";
    }

    /**
     * 툭정 채팅방 조회
     */
    @GetMapping("/room")
    public ResponseEntity roomInfo(@RequestBody ChatRoomDto roomDto) {
        log.info("# Get Chat Room, roomId: {}", roomDto.getRoomId());

        try {
            ChatRoom chatRoom = chatService.findRoomById(roomDto.getRoomId());
            return ResponseEntity.ok(chatRoom);
        } catch (Exception e) {
            return (ResponseEntity) ResponseEntity.badRequest();
        }
    }
}