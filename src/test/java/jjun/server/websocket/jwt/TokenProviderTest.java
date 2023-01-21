package jjun.server.websocket.jwt;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Test
    public void createAndValidToken() {
        String id = "/sub/chat/room/3f0f893a-5849-4028-9755-8c6c8ab1846b";
        int lastIdx = id.lastIndexOf("/");
        if (lastIdx != -1) {
            id = id.substring(lastIdx+1);
        }
        System.out.println(id);
    }


}