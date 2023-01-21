package jjun.server.websocket.config;

import jjun.server.websocket.dto.ChatMessage;
import jjun.server.websocket.jwt.TokenProvider;
import jjun.server.websocket.repository.ChatRoomRepository;
import jjun.server.websocket.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class StompHandler implements ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatService chatService;

    /**
     * WebSocket 을 통해 들어온 요청 처리 전에 수행되는 메소드
     * -> WebSocket 연결 시, 요청 헤더의 토큰 유효성을 검증
     *
     * * 유효하지 않은 JWT 토큰 셋팅 시에는 WebSocket 연결을 하지 않고 에외처리됨
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        // WebSocket 연결 시, 헤더의 JWT Token 검증
        if (StompCommand.CONNECT == accessor.getCommand()) {   // 채팅룸 연결 요청

            String accessToken = accessor.getFirstNativeHeader("token");
            log.info("CONNECT {}", accessToken);
            tokenProvider.validateToken(accessToken);

        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {   // 채팅룸 구독 요청

            // 세션 정보(sessionId) + 채팅방 정보(roomId) 를 조합하여 캐시에 저장 -> for 특정 클라이언트 세션이 어떤 채팅방에 들어가 있는지 알기 위함
            String roomId = chatService.getRoomId(Optional.ofNullable(
                    (String) message.getHeaders()  // 헤더에서 구독 Destination 정보를 얻고, roomId 추출
                            .get("simpDestination")).orElse("InvalidRoomId"));
            String sessionId = (String) message.getHeaders().get("simpSessionId");

            chatRoomRepository.setUserEnterInfo(sessionId, roomId);
            chatRoomRepository.plusUserCount(roomId);  // 인원 수 +1

            // 클라이언트의 입장 메시지 채팅방에 발송 -> 입/퇴장 안내는 서버에서 일괄적으로 처리 : Redis Publish
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            chatService.sendChatMessage(ChatMessage.builder()
                    .type(ChatMessage.MessageType.ENTER)
                    .roomId(roomId)
                    .sender(name)
                    .build());
            log.info("SUBSCRIBED {}, {}", name, roomId);

        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {  // WebSocket 연결 종료

            // 연결이 종료된 클라이언트 sessionId 로 roomId 조회
            String sessionId = (String) message.getHeaders().get("simpSessionId");
            String roomId = chatRoomRepository.getUserEnterRoomId(sessionId);

            chatRoomRepository.minusUserCount(roomId);  // 인원 수 -1

            // 클라이언트의 퇴장 메시지 채팅방에 발송 -> 이후 퇴장한 클라이언트의 roomId 매핑 정보 삭제 : Redis Publish
            String name = Optional.ofNullable((Principal) message.getHeaders().get("simpUser")).map(Principal::getName).orElse("UnknownUser");
            chatService.removeUserEnterInfo(sessionId);
            log.info("DISCONNECTED {}, {}", sessionId, roomId);

        }
        return message;
    }
}
