package jjun.server.websocket.repository;

import jjun.server.websocket.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertRoomInventories(List<ChatRoom> chatList) {
        String sql = "INSERT INTO chats" +
                "(message, users, house_id, created_at) VALUES(?,?,?,?)";
    }
}
