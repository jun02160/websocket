package jjun.server.websocket.dto.request;

import jjun.server.websocket.entity.ChatRoom;
import lombok.*;

import java.util.List;

/**
 * 채팅 메시지 타입 : 채팅방 입장(ENTER), 대화하기(TALK), 퇴장(QUIT)
 * - ENTER : 처음 채팅방에 들어오는 상태
 * - TALK : 이미 세션에 연결되어 있고 채팅중인 상태
 * - QUIT : 채팅방을 나가는 상태
 *
 * => 인원 수 표시 가능, 입장/퇴장 알림 서버에서 처리 가능
 */
@Getter @Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageSaveDto {


    public enum MessageType {
        ENTER, TALK, QUIT
    }

    private MessageType type;   // 메시지 유형
    private String roomId;   // 방 번호
    private String sender;   // 발신자
    private String nickname;  // 발신자의 닉네임
    private String message;  // 메시지 데이터
    private String createdAt; // 발신일자
    private List<String> userList;
//    private long userCount;  // 채팅방 인원 수: 채팅방 내에서 메시지가 전달될 때 인원 수 갱신

    public static ChatMessageSaveDto of(ChatRoom chatRoom) {
        return ChatMessageSaveDto.builder()
                .type(MessageType.TALK)
                .roomId(chatRoom.getHouse().getHouseId().toString())
                .sender(chatRoom.getUsers())
                .createdAt(chatRoom.getCreatedAt())
                .message(chatRoom.getMessage())
                .build();
    }

    public static ChatMessageSaveDto createChatMessageSaveDto(ChatMessageSaveDto messageSaveDto) {
        return ChatMessageSaveDto.builder()
                .type(MessageType.TALK)
                .roomId(messageSaveDto.getRoomId())
                .sender(messageSaveDto.getSender())
                .createdAt(messageSaveDto.getCreatedAt())
                .message(messageSaveDto.getMessage())
                .build();
    }
}

