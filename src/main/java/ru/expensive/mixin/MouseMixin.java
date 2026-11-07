package ru.expensive.mixin;

import aethereal.event.ChangeLookDirectionEvent;
import aethereal.event.MouseButtonEvent;
import aethereal.type.ButtonAction;
import aethereal.event.FovEvent;
import aethereal.Expensive;
import aethereal.event.VelocityEvent;
import aethereal.event.MouseButtonEvent2;
import aethereal.event.CameraPerspectiveEvent;
import aethereal.type.Mc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Mouse.class})
public class MouseMixin {

    @Shadow
    @Final
    private MinecraftClient field_1779;

    @Shadow
    public double field_1789;

    @Shadow
    public double field_1787;

    @Inject(method = {"onMouseScroll"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getInventory()Lnet/minecraft/entity/player/PlayerInventory;")}, cancellable = true)
    public void onMouseScrollHook(long j, double d, double d2, CallbackInfo callbackInfo) {
        VelocityEvent class238Var = new VelocityEvent(d, d2);
        Expensive.INSTANCE.eventDispatcher().dispatch(class238Var);
        if (class238Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"updateMouse"}, at = {@At("HEAD")})
    private void onUpdateMouse(double d, CallbackInfo callbackInfo) {
        FovEvent class146Var = new FovEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class146Var);
        if (class146Var.isCancelled()) {
            double fov = ((double) class146Var.getFov()) / ((double) ((Integer) (Object) this.field_1779.options.getFov().getValue()).intValue());
            this.field_1789 *= fov;
            this.field_1787 *= fov;
        }
    }

    @Inject(method = {"updateMouse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V")}, cancellable = true)
    private void onTurn(CallbackInfo callbackInfo) {
        CameraPerspectiveEvent class380Var = new CameraPerspectiveEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class380Var);
        if (class380Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @WrapWithCondition(method = {"updateMouse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V")}, require = 1, allow = 1)
    private boolean modifyMouseRotationInput(ClientPlayerEntity clientPlayerEntity, double d, double d2) {
        ChangeLookDirectionEvent class026Var = new ChangeLookDirectionEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class026Var);
        if (class026Var.isCancelled()) {
            return false;
        }
        clientPlayerEntity.changeLookDirection(d, d2);
        return false;
    }

    @Inject(method = {"onMouseButton"}, at = {@At("HEAD")})
    private void onMouseButton(long j, int i, int i2, int i3, CallbackInfo callbackInfo) {
        ButtonAction class108Var;
        Mc class815Var = Mc.INSTANCE;
        if (j == class815Var.getWindow().getHandle()) {
            switch (i2) {
                case 0:
                    class108Var = ButtonAction.RELEASE;
                    break;
                case 1:
                    class108Var = ButtonAction.PRESS;
                    break;
                default:
                    class108Var = null;
                    break;
            }
            ButtonAction class108Var2 = class108Var;
            if (class108Var2 == null || i == -1) {
                return;
            }
            if (class815Var.getCurrentScreen() == null) {
                Expensive.INSTANCE.eventDispatcher().dispatch(new MouseButtonEvent(class108Var2, i));
            } else {
                Expensive.INSTANCE.eventDispatcher().dispatch(new MouseButtonEvent2(class108Var2, i));
            }
        }
    }
}
