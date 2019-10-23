package com.xxx.xcloud.websocket.docker;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Interceptor for get param from webSocket session .
 * <p>
 * Such as ip,containerId,width,height,tenantName,podName,appType. some is
 * remaining.
 *
 * @author xujiangpeng
 */
public class ContainerHandshakeInterceptor extends HttpSessionHandshakeInterceptor {

    private String wsString = "Sec-WebSocket-Extensions";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        // Message compression.
        if (request.getHeaders().containsKey(wsString)) {
            request.getHeaders().set(wsString, "permessage-deflate");
        }

        String ip = ((ServletServerHttpRequest) request).getServletRequest().getParameter("ip");
        String containerId = ((ServletServerHttpRequest) request).getServletRequest().getParameter("containerId");
        String width = ((ServletServerHttpRequest) request).getServletRequest().getParameter("width");
        String height = ((ServletServerHttpRequest) request).getServletRequest().getParameter("height");
        String tenantName = ((ServletServerHttpRequest) request).getServletRequest().getParameter("tenantName");
        String podName = ((ServletServerHttpRequest) request).getServletRequest().getParameter("podName");
        String appType = ((ServletServerHttpRequest) request).getServletRequest().getParameter("apptype");
        String relationSign = ((ServletServerHttpRequest) request).getServletRequest().getParameter("relationSign");

        attributes.put("podName", podName);
        attributes.put("tenantName", tenantName);
        attributes.put("apptype", appType);
        attributes.put("relationSign", relationSign);
        attributes.put("ip", ip);
        attributes.put("containerId", containerId);
        attributes.put("width", width);
        attributes.put("height", height);
        return super.beforeHandshake(request, response, wsHandler, attributes);
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception ex) {
        super.afterHandshake(request, response, wsHandler, ex);
    }
}
