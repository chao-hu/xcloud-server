package com.xxx.xcloud.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.xxx.xcloud.common.Global;
import com.xxx.xcloud.websocket.docker.ContainerHandshakeInterceptor;
import com.xxx.xcloud.websocket.docker.ContainerWebSocketHandler;

/**
 * WebSocket config.
 *
 * @author xujiangpeng
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Bean
    public ContainerWebSocketHandler containerExecWSHandler() {
        return new ContainerWebSocketHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(containerExecWSHandler(), Global.DOCKER_WEBSOCKET_INTERCEPTURL)
                .addInterceptors(new ContainerHandshakeInterceptor()).setAllowedOrigins("*");

    }
}
