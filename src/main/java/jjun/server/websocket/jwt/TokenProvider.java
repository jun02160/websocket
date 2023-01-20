package jjun.server.websocket.jwt;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
public class TokenProvider {

    @Value("${spring.jwt.secret}")
    private String secretKey;

    private Long tokenValidMilisecond = 1000L * 60 * 60;  // 토큰 만료 시간: 1시간

    // 이름으로 JWT 토큰 생성
    public String generateToken(String name) {
        Date now = new Date();
        return Jwts.builder()
                .setId(name)
                .setIssuedAt(now)  // 토큰 발행일자
                .setExpiration(new Date(now.getTime() + tokenValidMilisecond))  // 토큰 만료시간
                .signWith(SignatureAlgorithm.HS256, secretKey)  // 암호화 알고리즘을 이용하여 Secret 값 셋팅
                .compact();
    }

    // JWT 토큰을 복호화하여 이름을 얻는다.
    public String getUserNameFromJwt(String jwt) {
        return getClaims(jwt).getBody().getId();
    }

    // Jwt 토큰 유효성 검사
    public boolean validateToken(String jwt) {
        return this.getClaims(jwt) != null;
    }

    private Jws<Claims> getClaims(String jwt) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwt);
        } catch (SignatureException e) {
            log.error("잘못된 JWT 서명입니다.");
            throw e;
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 토큰입니다.");
            throw e;
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 JWT 토큰입니다.");
            throw e;
        } catch (IllegalArgumentException e) {
            log.error("JWT 클레임이 비어있습니다.");
            throw e;
        }
    }
}
