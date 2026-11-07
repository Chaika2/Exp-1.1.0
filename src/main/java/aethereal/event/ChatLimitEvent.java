package aethereal.event;
import aethereal.type.ChatLimitType;

public class ChatLimitEvent extends CancellableEvent {
    public final ChatLimitType type;

    public ChatLimitType getType() {
        return this.type;
    }

    public ChatLimitEvent(ChatLimitType class057Var) {
        this.type = class057Var;
    }
}
