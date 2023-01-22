package jjun.server.websocket.repository;

import jjun.server.websocket.entity.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsertRoomInventories(List<ChatRoom> chatList) {
        String sql = "INSERT INTO chats" +
                "(message, users, house_id, created_at) VALUES(?,?,?,?)";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ChatRoom chatRoom = chatList.get(i);
                ps.setString(1, chatRoom.getMessage());
                ps.setString(2, chatRoom.getUsers());
                ps.setLong(3, chatRoom.getHouse().getHouseId());
                ps.setString(4, chatRoom.getCreatedAt());
            }

            @Override
            public int getBatchSize() {
                return chatList.size();
            }
        });
    }
}
