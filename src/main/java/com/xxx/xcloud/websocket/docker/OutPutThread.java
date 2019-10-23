package com.xxx.xcloud.websocket.docker;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Get input from ws terminal.
 *
 * @author xujiangpeng
 */
public class OutPutThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(OutPutThread.class);

    private InputStream inputStream;
    private WebSocketSession session;

    public OutPutThread(InputStream inputStream, WebSocketSession session) {
        super("OutPut" + System.currentTimeMillis());
        this.session = session;
        this.inputStream = inputStream;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            byte[] bytes = new byte[1024];
            while (!isInterrupted()) {
                int n = inputStream.read(bytes);
                String msg = new String(bytes, 0, n, "UTF8");
                session.sendMessage(new TextMessage(msg));
                bytes = new byte[1024];
            }
        } catch (Exception e) {
            logger.error("Web terminal I/O transform exception!", e);
        }
    }
}
