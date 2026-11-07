package ru.expensive.mixin;

import aethereal.event.ChatLimitEvent;
import aethereal.type.ChatLimitType;
import aethereal.accessor.ChatLineIdAccessor;
import aethereal.Expensive;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ChatHud.class})
public abstract class ChatHudMixin {

    @Shadow
    @Final
    public List<ChatHudLine.Visible> field_2064;

    @Shadow
    @Final
    public List<ChatHudLine> field_2061;

    @Shadow
    private boolean field_2067;

    @Shadow
    private int field_2066;

    @Shadow
    public abstract void method_1802(int i);

    @Shadow
    public abstract boolean method_1819();

    @Shadow
    public abstract int method_1811();

    @Redirect(method = {"addMessage(Lnet/minecraft/client/gui/hud/ChatHudLine;)V"}, at = @At(value = "INVOKE", target = "Ljava/util/List;size()I", ordinal = 0))
    public int hookGetSize2(List<ChatHudLine.Visible> list) {
        ChatLimitEvent class056Var = new ChatLimitEvent(ChatLimitType.LIMIT);
        Expensive.INSTANCE.eventDispatcher().dispatch(class056Var);
        if (class056Var.isCancelled()) {
            return -1;
        }
        return list.size();
    }

    @Inject(method = {"addVisibleMessage"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;isChatFocused()Z", shift = At.Shift.BEFORE)}, cancellable = true)
    public void hookAddVisibleMessage(ChatHudLine chatHudLine, CallbackInfo callbackInfo, @Local List<OrderedText> list) {
        boolean zMethod_1819 = method_1819();
        String strExpensive_ru$getId = ((ChatLineIdAccessor) ChatLineIdAccessor.class.cast(chatHudLine)).expensive_ru$getId();
        int i = 0;
        while (i < list.size()) {
            OrderedText orderedText = list.get(i);
            if (zMethod_1819 && this.field_2066 > 0) {
                this.field_2067 = true;
                method_1802(1);
            }
            ChatHudLine.Visible visible = new ChatHudLine.Visible(chatHudLine.creationTick(), orderedText, chatHudLine.indicator(), i == list.size() - 1);
            ((ChatLineIdAccessor) ChatLineIdAccessor.class.cast(visible)).expensive_ru$setId(strExpensive_ru$getId);
            this.field_2064.addFirst(visible);
            i++;
        }
        ChatLimitEvent class056Var = new ChatLimitEvent(ChatLimitType.LIMIT);
        Expensive.INSTANCE.eventDispatcher().dispatch(class056Var);
        if (!class056Var.isCancelled()) {
            while (this.field_2064.size() > 100) {
                this.field_2064.removeLast();
            }
        }
        callbackInfo.cancel();
    }

    @Inject(method = {"clear"}, at = {@At("HEAD")}, cancellable = true)
    private void onClear(boolean z, CallbackInfo callbackInfo) {
        ChatLimitEvent class056Var = new ChatLimitEvent(ChatLimitType.HISTORY);
        Expensive.INSTANCE.eventDispatcher().dispatch(class056Var);
        if (class056Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }
}
