package org.jubensha.aijubenshabackend.websocket.message;

import lombok.Data;

/**
 * WebSocket 消息
 */
@Data
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 发送者ID (GamePlayerId)
     */
    private Long sender;

    /**
     * 消息内容
     */
    private Object payload;

    public WebSocketMessage() {
    }

    public WebSocketMessage(String type, Long sender, Object payload) {
        this.type = type;
        this.sender = sender;
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
