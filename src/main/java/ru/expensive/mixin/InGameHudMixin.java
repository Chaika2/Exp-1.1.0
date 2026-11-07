package ru.expensive.mixin;

import aethereal.event.PerspectiveEvent;
import aethereal.Expensive;
import aethereal.event.CrosshairRenderEvent;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import aethereal.event.Render2DEvent;
import aethereal.type.Render2DStage;
import aethereal.event.StatusEffectOverlayEvent;
import aethereal.module.SaturationBarModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({InGameHud.class})
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient field_2035;

    @ModifyExpressionValue(method = {"tick()V"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;getMainHandStack()Lnet/minecraft/item/ItemStack;")})
    private ItemStack injectSilent(ItemStack itemStack) {
        return this.field_2035.player != null ? (ItemStack) (Object) this.field_2035.player.getInventory().main.get(Expensive.INSTANCE.inventoryService().hotbarSlotSwapper().getClientsideSlot()) : itemStack;
    }

    private static final int SATURATION_ICON_COUNT = 10;
    private static final int SATURATION_GOLD = 0xFFFFC85A;

    private int aethereal$satRight;
    private int aethereal$satTop;
    private boolean aethereal$satAreaValid;

    @Inject(method = {"renderFood"}, at = {@At("RETURN")})
    private void renderSaturationBar(DrawContext drawContext, PlayerEntity player, int top, int right, CallbackInfo callbackInfo) {
        SaturationBarModule saturationBarModule = Expensive.INSTANCE.moduleRepository().find(SaturationBarModule.class).orElse(null);
        if (saturationBarModule == null || !saturationBarModule.isState()) {
            aethereal$satAreaValid = false;
            return;
        }
        HungerManager hungerManager = player.getHungerManager();
        float saturation = hungerManager.getSaturationLevel();
        if (saturation <= 0.0f) {
            aethereal$satAreaValid = false;
            return;
        }
        this.aethereal$satRight = right;
        this.aethereal$satTop = top;
        this.aethereal$satAreaValid = true;
        saturationBarModule.render(drawContext, player, right, top);
    }

    private void renderGoldenFood(DrawContext drawContext, float saturation, int top, int right) {
        float icons = saturation / 2.0f;
        int count = Math.min((int) Math.ceil(icons), SATURATION_ICON_COUNT);
        Identifier full = Identifier.ofVanilla("hud/food_full");
        Identifier half = Identifier.ofVanilla("hud/food_half");
        for (int j = 0; j < count; j++) {
            int x = right - j * 8 - 9;
            boolean partial = j == count - 1 && icons < (float) (j + 1);
            drawContext.drawGuiTexture(RenderLayer::getGuiTexturedOverlay, partial ? half : full, x, top, 9, 9, SATURATION_GOLD);
        }
    }

    private void renderFoodOutline(DrawContext drawContext, float saturation, int top, int right) {
        float icons = saturation / 2.0f;
        int count = Math.min((int) Math.ceil(icons), SATURATION_ICON_COUNT);
        if (count <= 0) {
            return;
        }
        int left = right - (count - 1) * 8 - 9;
        int startX = left - 1;
        int endX = right + 1;
        int startY = top - 1;
        int endY = top + 10;
        drawContext.fill(startX, startY, endX, startY + 1, SATURATION_GOLD);
        drawContext.fill(startX, endY - 1, endX, endY, SATURATION_GOLD);
        drawContext.fill(startX, startY, startX + 1, endY, SATURATION_GOLD);
        drawContext.fill(endX - 1, startY, endX, endY, SATURATION_GOLD);
    }

    private void renderFoodBar(DrawContext drawContext, float saturation, int top, int right) {
        int barWidth = 81;
        int barHeight = 2;
        int barX = right - barWidth;
        int barY = top - barHeight - 2;
        drawContext.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, 0x66000000);
        drawContext.fill(barX, barY, barX + barWidth, barY + barHeight, 0x80000000);
        int filled = Math.round(barWidth * Math.min(saturation / 20.0f, 1.0f));
        if (filled > 0) {
            drawContext.fillGradient(barX, barY, barX + filled, barY + barHeight, 0xFFFFE082, 0xFFFFB300);
        }
    }

    @Shadow
    protected abstract void method_1757(DrawContext drawContext, ScoreboardObjective scoreboardObjective);



    @Inject(method = {"render"}, at = {@At("HEAD")})
    private void preRenderSat(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
        SaturationBarModule saturationBarModule = Expensive.INSTANCE.moduleRepository().find(SaturationBarModule.class).orElse(null);
        if (saturationBarModule != null && saturationBarModule.isState()) {
            saturationBarModule.updateTick();
            saturationBarModule.updatePartialTicks(renderTickCounter.getTickDelta(false));
        }
        Expensive.INSTANCE.eventDispatcher().dispatch(new Render2DEvent(drawContext.getMatrices(), Render2DStage.PRE, renderTickCounter, drawContext));
    }

    @Inject(method = {"renderCrosshair"}, at = {@At("HEAD")}, cancellable = true)
    private void crosshair(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
        CrosshairRenderEvent class247Var = new CrosshairRenderEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class247Var);
        if (class247Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"render"}, at = {@At("RETURN")})
    private void postRender(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new Render2DEvent(drawContext.getMatrices(), Render2DStage.POST, renderTickCounter, drawContext));
    }

    @ModifyExpressionValue(method = {"renderCrosshair"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getPerspective()Lnet/minecraft/client/option/Perspective;")})
    private Perspective hookPerspectiveEventOnCrosshair(Perspective perspective) {
        PerspectiveEvent class160Var = new PerspectiveEvent(perspective);
        Expensive.INSTANCE.eventDispatcher().dispatch(class160Var);
        return class160Var.perspective();
    }

    @ModifyExpressionValue(method = {"renderMiscOverlays"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getPerspective()Lnet/minecraft/client/option/Perspective;")})
    private Perspective hookPerspectiveEventOnMiscOverlays(Perspective perspective) {
        PerspectiveEvent class160Var = new PerspectiveEvent(perspective);
        Expensive.INSTANCE.eventDispatcher().dispatch(class160Var);
        return class160Var.perspective();
    }

    @Inject(method = {"renderStatusEffectOverlay"}, at = {@At("HEAD")}, cancellable = true)
    private void renderStatusEffectOverlay(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
        StatusEffectOverlayEvent class315Var = new StatusEffectOverlayEvent();
        Expensive.INSTANCE.eventDispatcher().dispatch(class315Var);
        if (class315Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"renderHotbar"}, at = {@At("HEAD")}, cancellable = true)
    private void renderHotbar(DrawContext drawContext, RenderTickCounter renderTickCounter, CallbackInfo callbackInfo) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.HOTBAR);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"renderHeldItemTooltip"}, at = {@At("HEAD")}, cancellable = true)
    private void renderHeldItemTooltip(DrawContext drawContext, CallbackInfo callbackInfo) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.HELD_ITEM_TOOLTIP);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Redirect(method = {"renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V"))
    private void renderScoreboard(InGameHud inGameHud, DrawContext drawContext, ScoreboardObjective scoreboardObjective) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.SCOREBOARD);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            return;
        }
        method_1757(drawContext, scoreboardObjective);
    }
}
