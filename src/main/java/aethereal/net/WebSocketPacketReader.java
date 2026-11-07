package aethereal.net;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketPacketReader {
    void read(SocketPacket class716Var, WebSocketSession webSocketSession);
}
