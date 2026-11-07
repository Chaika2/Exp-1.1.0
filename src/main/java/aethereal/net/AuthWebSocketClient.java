package aethereal.net;

import java.net.URI;
import java.nio.ByteBuffer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class AuthWebSocketClient extends WebSocketClient {
    public final WebSocketPacketReader packetReader;
    public final String authToken;

    public AuthWebSocketClient(URI uri, WebSocketPacketReader class715Var, String str) {
        super(uri);
        this.packetReader = class715Var;
        this.authToken = str;
    }

    public void onMessage(ByteBuffer byteBuffer) {
        try {
            this.packetReader.read(PacketCodec.fromBytes(byteBuffer.array()), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onOpen(ServerHandshake serverHandshake) {
        sendPacket(new AuthHandshakePacket(this.authToken));
    }

    public void sendPacket(SocketPacket class716Var) {
        send(PacketCodec.sendData(class716Var));
    }

    public void onMessage(String str) {
    }

    public void onClose(int i, String str, boolean z) {
    }

    public void onError(Exception exc) {
    }
}
