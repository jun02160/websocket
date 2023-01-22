package jjun.server.websocket.repository;

import jjun.server.websocket.entity.House;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HouseRepository extends JpaRepository<House, Long> {

}
