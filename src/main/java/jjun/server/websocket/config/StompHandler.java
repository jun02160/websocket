package jjun.server.websocket.config;

import jjun.server.websocket.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Controller;

@Slf4j
@RequiredArgsConstructor
@Controller
public class StompHandler implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

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
        if (StompCommand.CONNECT == accessor.getCommand()) {
            tokenProvider.validateToken(accessor.getFirstNativeHeader("token"));
        }
        return message;
    }
}
