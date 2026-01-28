package org.jubensha.aijubenshabackend.websocket.message;

import lombok.Data;

@Data
public class GameMessage {
    
    private Long gameId;
    private WebSocketMessage message;
}
