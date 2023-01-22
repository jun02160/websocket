package jjun.server.websocket.entity;

import jjun.server.websocket.dto.request.ChatMessageSaveDto;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.UUID;

/**
 * 채팅방 Entity
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "chats")
public class ChatRoom implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

//    @Column
//    private String roomId;

    @Column
    private String message;

    @Column
    private String users;

    @Column
    private String createdAt;

    @ManyToOne
    @JoinColumn(name = "house_id", nullable = false)
    private House house;

//    private long userCount;  // 채팅방 인원 수

    public static ChatRoom of(ChatMessageSaveDto chatMessageSaveDto, House house) {
        return ChatRoom.builder()
                .message(chatMessageSaveDto.getMessage())
                .createdAt(chatMessageSaveDto.getCreatedAt())
                .users(chatMessageSaveDto.getSender())
                .house(house)
                .build();
    }
}

