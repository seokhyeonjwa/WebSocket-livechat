package hongfolio.hongchat.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class PublicChatHandler extends TextWebSocketHandler {

    private static final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        session.getAttributes().put("userId", userId);
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sender = (String) session.getAttributes().get("userId");

        // 클라이언트에서 보낸 JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> incoming = objectMapper.readValue(message.getPayload(), Map.class);

        String content = incoming.get("content");
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 새로운 JSON 메시지 구성
        String jsonMessage = objectMapper.writeValueAsString(Map.of(
                "sender", sender,
                "content", content,
                "timestamp", timestamp
        ));

        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage(jsonMessage));
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    private String extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        return (uri != null && uri.getQuery() != null && uri.getQuery().contains("user="))
                ? uri.getQuery().split("user=")[1]
                : "익명";
    }
}
