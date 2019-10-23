package com.xxx.xcloud.websocket.docker;

import java.net.Socket;

/**
 * Entity for distinct WebSocket Session.
 *
 * @author xujiangpeng
 */
public class ExecSession {

    private String ip;
    private String containerId;
    private Socket socket;
    private OutPutThread outPutThread;

    public ExecSession(String ip, String containerId, Socket socket, OutPutThread outPutThread) {
        this.ip = ip;
        this.containerId = containerId;
        this.socket = socket;
        this.outPutThread = outPutThread;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public OutPutThread getOutPutThread() {
        return outPutThread;
    }

    public void setOutPutThread(OutPutThread outPutThread) {
        this.outPutThread = outPutThread;
    }
}
