package aethereal.event;
import aethereal.util.ChatDecorator;
import aethereal.type.ChatMessageType;

import net.minecraft.text.Text;

public class ChatReceiveEvent extends CancellableEvent {
    public final String message;
    public final Text textData;

    public final ChatMessageType type;

    public final ChatDecorator applyChatDecoration;

    public String message() {
        return this.message;
    }

    public Text textData() {
        return this.textData;
    }

    public ChatMessageType type() {
        return this.type;
    }

    public ChatDecorator applyChatDecoration() {
        return this.applyChatDecoration;
    }

    public ChatReceiveEvent(String str, Text text, ChatMessageType class068Var, ChatDecorator class067Var) {
        this.message = str;
        this.textData = text;
        this.type = class068Var;
        this.applyChatDecoration = class067Var;
    }
}
