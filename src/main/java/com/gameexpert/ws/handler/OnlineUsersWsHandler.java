package com.gameexpert.ws.handler;

import java.util.ArrayList;
import java.util.List;
import com.gameexpert.ws.NicknameHandshakeInterceptor;
import com.gameexpert.ws.WorldBroadcaster;
import com.gameexpert.ws.WorldSessionRegistry;
import com.gameexpert.ws.WsMessageContext;
import com.gameexpert.ws.dto.OnlineUsersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class OnlineUsersWsHandler implements WsMessageHandler {
    private final WorldSessionRegistry registry;
    private final WorldBroadcaster broadcaster;

    @Override
    public String type() {
        return "onlineUsers";
    }

    @Override
    public void handle(WsMessageContext context, JsonNode message) {
        // TODO Lv 15: 현재 월드의 열린 연결에서 닉네임을 조회하고 요청자에게 응답합니다.
        List<String> users = new ArrayList<>();

        for (WorldSessionRegistry.Entry entry
                : registry.entries(context.worldId())) {

            var session = entry.session();

            if (!session.isOpen()) {
                continue;
            }

            String nickname = (String) session.getAttributes()
                    .get(NicknameHandshakeInterceptor.ATTR_NICKNAME);

            users.add(nickname);
        }

        users.sort(String::compareTo);

        int count = users.size();

        OnlineUsersResponse response =
                new OnlineUsersResponse(users, count);

        broadcaster.sendTo(context.session(), response);
    }
}
