package com.example.revly.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");  // Prefixes for broadcast (topic) and private (queue) messages
        config.setApplicationDestinationPrefixes("/app");  // Prefix for client-sent messages
        config.setUserDestinationPrefix("/user");  // For user-specific messaging
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-chat")  // WebSocket endpoint clients connect to
                .setAllowedOriginPatterns("*")  // Adjust for your frontend origins
                .withSockJS();  // Fallback for browsers without WebSocket support
    }
}
