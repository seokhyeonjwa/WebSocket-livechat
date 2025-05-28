package hongfolio.hongchat.config;

import hongfolio.hongchat.handler.ChatWebSocketHandler;
import hongfolio.hongchat.handler.PrivateChatHandler;
import hongfolio.hongchat.handler.PublicChatHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(publicChatHandler(), "/ws/public")
                .setAllowedOrigins("*");


      registry.addHandler(privateChatHandler(), "/ws/private")
                .setAllowedOrigins("*");
/**
        registry.addHandler(chatBotHandler(), "/ws/chatbot")
                .setAllowedOrigins("*");
     **/
    }


    @Bean
    public WebSocketHandler chatHandler() {
        return new ChatWebSocketHandler();
    }
    @Bean
    public PublicChatHandler publicChatHandler() {
        return new PublicChatHandler();
    }
    @Bean
    public PrivateChatHandler privateChatHandler() {
        return new PrivateChatHandler();
    }
}
