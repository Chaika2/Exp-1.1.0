package aethereal.util;
import aethereal.event.ChatSocketListener;
import aethereal.Expensive;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AshfieldChatHandler extends WebSocketClient {
    public static final String hmacSecret = "yQsBxaBFkBibJimhHsoMXELeEWUuZRPP";
    public String uid;
    public String username;
    public String avatarUrl;
    public String role;
    public String token;
    public ChatSocketListener listener;

    public AshfieldChatHandler(URI uri) {
        super(uri);
    }

    public void sendPing() {
        Expensive.LOGGER.info("[Ashfield Chat] sending ping");
        super.sendPing();
    }

    public void onOpen(ServerHandshake serverHandshake) throws JSONException {
        sendJoinMessage();
    }

    public void onMessage(String str) {
        try {
            JSONObject jSONObject = new JSONObject(str);
            switch (jSONObject.optString("type", "")) {
                case "join_response":
                    JSONObject jSONObject2 = jSONObject.getJSONObject("payload");
                    if (!jSONObject2.optBoolean("success", false)) {
                        String strOptString = jSONObject2.optString("error", "Join failed");
                        if (this.listener != null) {
                            this.listener.onError(strOptString);
                        }
                    } else if (this.listener != null) {
                        this.listener.onConnected();
                        JSONArray jSONArrayOptJSONArray = jSONObject2.optJSONArray("messageHistory");
                        if (jSONArrayOptJSONArray != null) {
                            this.listener.onMessageHistory(jSONArrayOptJSONArray);
                        }
                    }
                    break;
                case "new_message":
                    JSONObject jSONObjectOptJSONObject = jSONObject.optJSONObject("payload");
                    if (jSONObjectOptJSONObject != null && this.listener != null) {
                        System.out.println(jSONObjectOptJSONObject);
                        this.listener.onNewMessage(jSONObjectOptJSONObject);
                    }
                    break;
                case "user_count":
                    JSONObject jSONObjectOptJSONObject2 = jSONObject.optJSONObject("payload");
                    if (jSONObjectOptJSONObject2 != null && this.listener != null) {
                        this.listener.onUserCountUpdate(jSONObjectOptJSONObject2.optInt("count", 0));
                    }
                    break;
                case "error":
                    JSONObject jSONObjectOptJSONObject3 = jSONObject.optJSONObject("payload");
                    String strOptString2 = jSONObjectOptJSONObject3 != null ? jSONObjectOptJSONObject3.optString("message", "Unknown error") : "Unknown error";
                    if (this.listener != null) {
                        this.listener.onError(strOptString2);
                        break;
                    }
                    break;
            }
        } catch (Exception e) {
            Expensive.LOGGER.error("[Ashfield Chat] failed to parse message", e);
        }
    }

    public void onMessage(ByteBuffer byteBuffer) {
        onMessage(StandardCharsets.UTF_8.decode(byteBuffer).toString());
    }

    public void onClose(int i, String str, boolean z) {
        if (this.listener != null) {
            this.listener.onDisconnected();
        }
    }

    public void onError(Exception exc) {
        if (this.listener != null) {
            this.listener.onError(exc.getMessage());
        }
    }

    public void sendMessage(String str) {
        if (str == null || str.trim().isEmpty()) {
            if (this.listener != null) {
                this.listener.onError("Message cannot be empty");
            }
        } else if (str.length() > 150) {
            if (this.listener != null) {
                this.listener.onError("Message cannot exceed 150 characters");
            }
        } else {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", "send_message");
                jSONObject.put("content", str.trim());
                send(jSONObject.toString());
            } catch (Exception e) {
                this.listener.onError("Failed to send message");
            }
        }
    }

    public void sendJoinMessage() throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("type", "join");
        jSONObject.put("uid", this.uid);
        jSONObject.put("username", this.username);
        if (this.avatarUrl != null && !this.avatarUrl.isEmpty()) {
            jSONObject.put("avatarUrl", this.avatarUrl);
        }
        if (this.role != null && !this.role.isEmpty()) {
            jSONObject.put("role", this.role);
        }
        jSONObject.put("token", this.token);
        send(jSONObject.toString());
    }

    public void createUser(String str, String str2, String str3, String str4) {
        this.uid = str;
        this.username = str2;
        this.avatarUrl = str3;
        this.role = str4;
        this.token = generateToken(str2);
    }

    public static String generateToken(String str) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(hmacSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bArrDoFinal = mac.doFinal(str.toLowerCase().trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bArrDoFinal) {
                String hexString = Integer.toHexString(255 & b);
                if (hexString.length() == 1) {
                    sb.append('0');
                }
                sb.append(hexString);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public void setListener(ChatSocketListener class326Var) {
        this.listener = class326Var;
    }
}
