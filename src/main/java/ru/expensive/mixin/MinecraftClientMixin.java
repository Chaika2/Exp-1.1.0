package ru.expensive.mixin;

import aethereal.util.StencilBufferUtil;
import aethereal.event.UseItemEvent;
import aethereal.event.StopUsingItemEvent;
import aethereal.event.WorldLoadEvent;
import aethereal.event.FrameRenderEvent;
import aethereal.event.ScreenOpenEvent;
import aethereal.Expensive;
import aethereal.event.BlockInteractEvent;
import aethereal.event.ClientInitEvent;
import aethereal.event.ClientTickEvent;
import aethereal.type.Mc;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({MinecraftClient.class})
public class MinecraftClientMixin {

    @Shadow
    @Nullable
    public Screen field_1755;

    @Shadow
    @Nullable
    public ClientWorld field_1687;

    @Shadow
    @Nullable
    private Overlay field_18175;

    @Shadow
    @Final
    private Window field_1704;

    @Unique
    private static boolean expensive$replacingScreen = false;

    @Unique
    private boolean expensive$windowTitleSet = false;

    @Inject(method = {"tick"}, at = {@At("HEAD")})
    private void onTick(CallbackInfo callbackInfo) {
        if (this.field_1704 != null) {
            this.field_1704.setTitle("ZortexDLC");
        }
        Expensive.INSTANCE.eventDispatcher().dispatch(new ClientTickEvent());
    }

    @WrapOperation(method = {"doItemUse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;interactBlock(Lnet/minecraft/client/network/ClientPlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;")})
    public ActionResult interactBlockHook(ClientPlayerInteractionManager clientPlayerInteractionManager, ClientPlayerEntity clientPlayerEntity, Hand hand, BlockHitResult blockHitResult, Operation<ActionResult> operation) {
        BlockInteractEvent class175Var = new BlockInteractEvent(blockHitResult, hand);
        Expensive.INSTANCE.eventDispatcher().dispatch(class175Var);
        return class175Var.isCancelled() ? ActionResult.FAIL : (ActionResult) operation.call(new Object[]{clientPlayerInteractionManager, clientPlayerEntity, hand, blockHitResult});
    }

    @ModifyExpressionValue(method = {"doItemUse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isRiding()Z")})
    public boolean doItemUseHook(boolean z) {
        UseItemEvent class059Var = new UseItemEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class059Var);
        if (class059Var.isCancelled()) {
            return true;
        }
        return z;
    }

    @Inject(method = {"setWorld"}, at = {@At("HEAD")})
    private void setWorld(ClientWorld clientWorld, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new WorldLoadEvent(clientWorld));
    }

    @Redirect(method = {"handleBlockBreaking"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean redirectHandleBlockBreaking(ClientPlayerEntity clientPlayerEntity) {
        StopUsingItemEvent class076Var = new StopUsingItemEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class076Var);
        if (class076Var.isCancelled()) {
            return false;
        }
        return clientPlayerEntity.isUsingItem();
    }

    @Inject(method = {"onResolutionChanged"}, at = {@At("RETURN")})
    public void onResolutionChanged(CallbackInfo callbackInfo) {
        StencilBufferUtil.initFramebuffer(this.field_1704.getFramebufferWidth(), this.field_1704.getFramebufferHeight(), Mc.INSTANCE.getFramebuffer().getColorAttachment());
    }

    @Inject(method = {"render"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/gl/Framebuffer;endWrite()V")})
    private void runTick(boolean z, CallbackInfo callbackInfo) throws MatchException {
        Expensive.INSTANCE.eventDispatcher().dispatch(new FrameRenderEvent());
        Expensive.INSTANCE.windowControllerAdapter().preBlitFramebufferToBackbuffer(Mc.INSTANCE.getWindow().getHandle());
    }

    @ModifyExpressionValue(method = {"doItemUse"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z")})
    private boolean modifyDoItemUse(boolean z) {
        StopUsingItemEvent class076Var = new StopUsingItemEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class076Var);
        if (class076Var.isCancelled()) {
            return false;
        }
        return z;
    }

    @Inject(method = {"setScreen"}, at = {@At("HEAD")}, cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo callbackInfo) {
        if (this.field_1704 != null) {
            this.field_1704.setTitle("ZortexDLC");
        }
        if (expensive$replacingScreen) {
            return;
        }
        expensive$replacingScreen = true;
        try {
            ScreenOpenEvent class135Var = new ScreenOpenEvent(screen);
            Expensive.INSTANCE.eventDispatcher().dispatch(class135Var);
            if (class135Var.isCancelled()) {
                callbackInfo.cancel();
            }
        } finally {
            expensive$replacingScreen = false;
        }
    }

    @Inject(at = {@At("HEAD")}, method = {"stop"})
    private void stop(CallbackInfo callbackInfo) {
        Expensive.INSTANCE.discordManager().stopRPC();
        Expensive.INSTANCE.configManager().saveSessionNickname();
    }

    @Inject(method = {"<init>"}, at = {@At("RETURN")})
    public void init(RunArgs runArgs, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new ClientInitEvent());
    }
}
