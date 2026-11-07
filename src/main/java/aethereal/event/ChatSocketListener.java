package aethereal.event;

import org.json.JSONArray;
import org.json.JSONObject;

public interface ChatSocketListener {
    default void onConnected() {
    }

    default void onDisconnected() {
    }

    default void onError(String str) {
    }

    default void onMessageHistory(JSONArray jSONArray) {
    }

    default void onNewMessage(JSONObject jSONObject) {
    }

    default void onUserCountUpdate(int i) {
    }
}
