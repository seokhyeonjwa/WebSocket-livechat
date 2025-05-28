package hongfolio.hongchat.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PrivateChatHandler extends TextWebSocketHandler {

    private static final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = extractUserId(session);
        session.getAttributes().put("userId", userId);
        userSessions.put(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sender = (String) session.getAttributes().get("userId");

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> incoming = objectMapper.readValue(message.getPayload(), Map.class);

        String content = incoming.get("content");
        String receiver = incoming.get("receiver");

        if (receiver == null || receiver.isBlank()) {
            System.out.println("❌ receiver가 null입니다. 메시지를 무시합니다.");
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String jsonMessage = objectMapper.writeValueAsString(Map.of(
                "sender", sender,
                "content", content,
                "timestamp", timestamp
        ));

        WebSocketSession receiverSession = userSessions.get(receiver);
        if (receiverSession != null && receiverSession.isOpen()) {
            receiverSession.sendMessage(new TextMessage(jsonMessage));
        }

        if (!receiver.equals(sender)) {
            session.sendMessage(new TextMessage(jsonMessage));
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = (String) session.getAttributes().get("userId");
        userSessions.remove(userId);
    }

    private String extractUserId(WebSocketSession session) {
        URI uri = session.getUri();
        return (uri != null && uri.getQuery() != null && uri.getQuery().contains("user="))
                ? uri.getQuery().split("user=")[1]
                : "익명";
    }
}

