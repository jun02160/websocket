package jjun.server.websocket.repository;

import jjun.server.websocket.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<ChatRoom, Long> {

    Slice<ChatRoom> findAllByCreatedAtBeforeAndHouse_IdOrderByCreatedDesc(String curserCreatedAt, Long houseId, Pageable pageable);

    List<ChatRoom> findAllByCreatedAtAfterOrderByCreatedAtDesc(String cursorCreatedAt);
}
