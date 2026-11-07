package ru.expensive.mixin;

import aethereal.event.ChatSendEvent;
import aethereal.Expensive;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({TextVisitFactory.class})
public class TextVisitFactoryMixin {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"}, index = 0)
    private static String visitFormatted(String str) {
        ChatSendEvent class093Var = new ChatSendEvent(str);
        Expensive.INSTANCE.eventDispatcher().dispatch(class093Var);
        return (!class093Var.isCancelled() || class093Var.getChangedText().isEmpty()) ? str : class093Var.getChangedText();
    }
}
