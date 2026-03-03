package org.jubensha.aijubenshabackend.websocket.message;

import lombok.Data;

/**
 * WebSocket 消息
 */
@Data
public class WebSocketMessage {

    private String type;
    private Object payload;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public void setType(MessageType messageType) {
        this.type = messageType.name();
    }

    public enum MessageType {
        CHAT_MESSAGE,
        SCRIPT_READY,
        START_INVESTIGATION,
        PUBLIC_CLUE,
        VOTE_RESULT
    }
}
