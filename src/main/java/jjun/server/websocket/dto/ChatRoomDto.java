package jjun.server.websocket.dto;

import lombok.*;

import java.io.Serializable;

@Getter @Setter
public class ChatRoomDto implements Serializable {

    private static final long serialVersionUID = 6494678977089006639L;

    private String houseId;
}
