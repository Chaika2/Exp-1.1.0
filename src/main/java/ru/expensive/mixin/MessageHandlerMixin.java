package ru.expensive.mixin;

import aethereal.event.ChatReceiveEvent;
import aethereal.type.ChatMessageType;
import aethereal.Expensive;
import com.mojang.authlib.GameProfile;
import java.time.Instant;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({MessageHandler.class})
public class MessageHandlerMixin {

    @Shadow
    private long field_39798;

    @Inject(method = {"method_45745"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/network/message/MessageType$Parameters;applyChatDecoration(Lnet/minecraft/text/Text;)Lnet/minecraft/text/Text;", shift = At.Shift.BEFORE)}, cancellable = true)
    private void injectDisguisedChatLambda(MessageType.Parameters parameters, Text text, Instant instant, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (expensive_ru$emitChatEvent(parameters, text, ChatMessageType.DISGUISED_CHAT_MESSAGE)) {
            this.field_39798 = Util.getMeasuringTimeMs();
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method = {"processChatMessageInternal"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", ordinal = 0, shift = At.Shift.BEFORE)}, cancellable = true)
    private void injectChatMessage1(MessageType.Parameters parameters, SignedMessage signedMessage, Text text, GameProfile gameProfile, boolean z, Instant instant, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (expensive_ru$emitChatEvent(null, text, ChatMessageType.CHAT_MESSAGE)) {
            this.field_39798 = Util.getMeasuringTimeMs();
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method = {"processChatMessageInternal"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", ordinal = 1, shift = At.Shift.BEFORE)}, cancellable = true)
    private void injectChatMessage2(MessageType.Parameters parameters, SignedMessage signedMessage, Text text, GameProfile gameProfile, boolean z, Instant instant, CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (expensive_ru$emitChatEvent(parameters, text, ChatMessageType.CHAT_MESSAGE)) {
            this.field_39798 = Util.getMeasuringTimeMs();
            callbackInfoReturnable.cancel();
        }
    }

    @Inject(method = {"onGameMessage"}, at = {@At("HEAD")}, cancellable = true)
    private void injectGameMessage(Text text, boolean z, CallbackInfo callbackInfo) {
        if (expensive_ru$emitChatEvent(null, text, z ? ChatMessageType.DISGUISED_CHAT_MESSAGE : ChatMessageType.GAME_MESSAGE)) {
            this.field_39798 = Util.getMeasuringTimeMs();
            callbackInfo.cancel();
        }
    }

    @Unique
    private boolean expensive_ru$emitChatEvent(MessageType.Parameters parameters, Text text, ChatMessageType class068Var) {
        ChatReceiveEvent class066Var = new ChatReceiveEvent(text.getString(), text, class068Var, text2 -> {
            return parameters != null ? parameters.applyChatDecoration(text) : text;
        });
        Expensive.INSTANCE.eventDispatcher().dispatch(class066Var);
        return class066Var.isCancelled();
    }
}
