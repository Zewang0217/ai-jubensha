package org.jubensha.aijubenshabackend.websocket.message;

import lombok.Data;

@Data
public class WebSocketMessage {

    private String type;
    private Object payload;
}
