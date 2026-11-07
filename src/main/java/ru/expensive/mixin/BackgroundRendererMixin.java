package ru.expensive.mixin;

import aethereal.Expensive;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import aethereal.event.VisualEffectEvent;
import aethereal.type.VisualEffectType;
import aethereal.module.WorldTweaksModule;
import aethereal.ui.setting.ColorSetting;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.util.stream.Stream;
import net.minecraft.block.enums.CameraSubmersionType;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BackgroundRenderer.class})
public class BackgroundRendererMixin {
    @ModifyExpressionValue(method = {"getFogModifier"}, at = {@At(value = "INVOKE", target = "Ljava/util/List;stream()Ljava/util/stream/Stream;")})
    private static Stream<BackgroundRenderer.StatusEffectFogModifier> modifyFogStream(Stream<BackgroundRenderer.StatusEffectFogModifier> stream) {
        return stream.filter(statusEffectFogModifier -> {
            VisualEffectType class259Var;
            StatusEffect statusEffect = (StatusEffect) statusEffectFogModifier.getStatusEffect().value();
            if (statusEffect == StatusEffects.BLINDNESS.value()) {
                class259Var = VisualEffectType.BLINDNESS;
            } else {
                if (statusEffect != StatusEffects.DARKNESS.value()) {
                    return true;
                }
                class259Var = VisualEffectType.DARKNESS;
            }
            VisualEffectEvent class258Var = new VisualEffectEvent(class259Var);
            Expensive.INSTANCE.eventDispatcher().dispatch(class258Var);
            return !class258Var.isCancelled();
        });
    }

    @Inject(method = {"getFogColor"}, at = {@At("HEAD")}, cancellable = true)
    private static void modifyFogColorArgs(Camera camera, float f, ClientWorld clientWorld, int i, float f2, CallbackInfoReturnable<Vector4f> callbackInfoReturnable) {
        WorldTweaksModule class636Var = (WorldTweaksModule) Expensive.INSTANCE.moduleRepository().get(WorldTweaksModule.class);
        if (camera.getSubmersionType() == CameraSubmersionType.LAVA || camera.getSubmersionType() == CameraSubmersionType.POWDER_SNOW || !class636Var.isState() || !class636Var.changeFogColor().isValue()) {
            return;
        }
        ColorSetting class667VarFogColor = class636Var.fogColor();
        callbackInfoReturnable.setReturnValue(new Vector4f(class667VarFogColor.getRed() / 255.0f, class667VarFogColor.getGreen() / 255.0f, class667VarFogColor.getBlue() / 255.0f, class667VarFogColor.getAlpha()));
    }

    @Inject(method = {"applyFog"}, at = {@At("HEAD")}, cancellable = true)
    private static void changeColor(Camera camera, BackgroundRenderer.FogType fogType, Vector4f vector4f, float f, boolean z, float f2, CallbackInfoReturnable<Fog> callbackInfoReturnable) {
        WorldTweaksModule class636Var = (WorldTweaksModule) Expensive.INSTANCE.moduleRepository().get(WorldTweaksModule.class);
        if (camera.getSubmersionType() == CameraSubmersionType.LAVA) {
            RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.LAVA_OVERLAY);
            Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
            if (class252Var.isCancelled()) {
                callbackInfoReturnable.setReturnValue(new Fog(100.0f, 103.0f, FogShape.CYLINDER, vector4f.x(), vector4f.y(), vector4f.z(), vector4f.w()));
                return;
            }
            return;
        }
        if (camera.getSubmersionType() != CameraSubmersionType.POWDER_SNOW && class636Var.isState() && class636Var.changeFogColor().isValue()) {
            ColorSetting class667VarFogColor = class636Var.fogColor();
            callbackInfoReturnable.setReturnValue(new Fog(2.0f, class636Var.fogDistance().currentValue(), FogShape.CYLINDER, class667VarFogColor.getRed() / 255.0f, class667VarFogColor.getGreen() / 255.0f, class667VarFogColor.getBlue() / 255.0f, class667VarFogColor.getAlpha()));
        }
    }
}
