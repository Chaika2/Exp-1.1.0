package aethereal.module;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.render.ColorStack;
import aethereal.event.CrosshairRenderEvent;
import aethereal.type.CrosshairStyle;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.Render2DEvent;
import aethereal.render.ScreenResolution;
import aethereal.type.SettingUnit;
import aethereal.util.StencilBufferUtil;

import java.util.Objects;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.util.hit.HitResult;
import org.joml.Matrix4f;

public class CrosshairModule extends Module {
    public final Mc mc;
    public final ModeSetting<CrosshairStyle> styleSetting;
    public final ColorSetting colorSetting;
    public final NumberSetting radiusSetting;
    public final NumberSetting gapSetting;
    public final NumberSetting lengthSetting;
    public final NumberSetting thicknessSetting;
    public final BooleanSetting cooldownSetting;
    public final NumberSetting cooldownMultiplierSetting;
    public final BooleanSetting outlineSetting;
    public final BooleanSetting tModeSetting;

    public final BooleanSetting centerDotSetting;
    public final NumberSetting centerSizeSetting;
    public float circleProgress;

    public CrosshairModule() {
        super(ModuleTab.RENDER, "Crosshair");
        this.mc = Mc.INSTANCE;
        this.styleSetting = new ModeSetting(Lang.CROSSHAIR_TYPE, Lang.CROSSHAIR_TYPE_DESC).values(CrosshairStyle.class);
        this.colorSetting = new ColorSetting(Lang.CROSSHAIR_COLOR, Lang.CROSSHAIR_COLOR_DESC).setColor(16777215);
        this.radiusSetting = new NumberSetting(Lang.CROSSHAIR_RADIUS, Lang.CROSSHAIR_RADIUS_DESC).currentValue(12.0f).range(1.0f, 20.0f).visible(() -> {
            return Boolean.valueOf(this.styleSetting.isSelected(CrosshairStyle.CIRCLE));
        }).unit(SettingUnit.PIXELS);
        this.gapSetting = new NumberSetting(Lang.CROSSHAIR_GAP, Lang.CROSSHAIR_GAP_DESC).currentValue(2.0f).range(0.0f, 20.0f).step(1.0f).visible(() -> {
            return Boolean.valueOf(this.styleSetting.isSelected(CrosshairStyle.DEFAULT));
        }).unit(SettingUnit.PIXELS);
        this.lengthSetting = new NumberSetting(Lang.CROSSHAIR_LENGTH, Lang.CROSSHAIR_LENGTH_DESC).currentValue(5.0f).range(0.0f, 20.0f).step(1.0f).visible(() -> {
            return Boolean.valueOf(this.styleSetting.isSelected(CrosshairStyle.DEFAULT));
        }).unit(SettingUnit.PIXELS);
        this.thicknessSetting = new NumberSetting(Lang.CROSSHAIR_THICKNESS, Lang.CROSSHAIR_THICKNESS_DESC).currentValue(2.0f).range(1.0f, 5.0f).step(1.0f).unit(SettingUnit.PIXELS);
        this.cooldownSetting = new BooleanSetting(Lang.CROSSHAIR_COOLDOWN, Lang.CROSSHAIR_COOLDOWN_DESC);
        NumberSetting class613VarStep = new NumberSetting(Lang.CROSSHAIR_COOLDOWN_MULTIPLIER, Lang.CROSSHAIR_COOLDOWN_MULTIPLIER_DESC).currentValue(8.0f).range(1.0f, 20.0f).step(1.0f);
        BooleanSetting class665Var = this.cooldownSetting;
        Objects.requireNonNull(class665Var);
        this.cooldownMultiplierSetting = class613VarStep.visible(class665Var::isValue);
        this.outlineSetting = new BooleanSetting(Lang.CROSSHAIR_OUTLINE, Lang.CROSSHAIR_OUTLINE_DESC).visible(() -> {
            return Boolean.valueOf(this.styleSetting.isSelected(CrosshairStyle.DEFAULT));
        });
        this.tModeSetting = new BooleanSetting(Lang.CROSSHAIR_TMODE, Lang.CROSSHAIR_TMODE_DESC).visible(() -> {
            return Boolean.valueOf(this.styleSetting.isSelected(CrosshairStyle.DEFAULT));
        });
        this.centerDotSetting = new BooleanSetting(Lang.CROSSHAIR_CENTER_DOT, Lang.CROSSHAIR_CENTER_DOT_DESC).visible(() -> {
            return Boolean.valueOf(this.styleSetting.isSelected(CrosshairStyle.DEFAULT));
        });
        this.centerSizeSetting = new NumberSetting(Lang.CROSSHAIR_CENTER_SIZE, Lang.CROSSHAIR_CENTER_SIZE_DESC).currentValue(2.0f).range(1.0f, 10.0f).step(0.05f).visible(() -> {
            return Boolean.valueOf(this.centerDotSetting.isValue() && this.styleSetting.isSelected(CrosshairStyle.DEFAULT));
        });
        this.circleProgress = 0.0f;
        addSettings(this.styleSetting, this.colorSetting, this.radiusSetting, this.gapSetting, this.lengthSetting, this.thicknessSetting, this.cooldownSetting, this.cooldownMultiplierSetting, this.outlineSetting, this.tModeSetting, this.centerDotSetting, this.centerSizeSetting);
        register(Render2DEvent.class, class311Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.mc.getGameOptions().getPerspective() == Perspective.FIRST_PERSON) {
                Matrix4f positionMatrix = class311Var.matrixStack().peek().getPositionMatrix();
                DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                ClientPlayerEntity player = this.mc.getPlayer();
                ScreenResolution class710VarResolution = ScreenResolution.resolution();
                float fScreenWidth = class710VarResolution.screenWidth() / 2.0f;
                float fScreenHeight = class710VarResolution.screenHeight() / 2.0f;
                float attackCooldownProgress = this.cooldownSetting.isValue() ? 1.0f - player.getAttackCooldownProgress(1.0f) : 0.0f;
                int iComputeColor = !(this.mc.getCrosshairTarget().getType() == HitResult.Type.ENTITY) ? class115VarColorStack.computeColor(this.colorSetting.getColor(), this.colorSetting.getAlpha()) : class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 55, 55);
                int iBlack = class115VarColorStack.black();
                class154VarDrawEngine.begin();
                switch ((CrosshairStyle) this.styleSetting.currentValue()) {
                    case DEFAULT:
                        float fCurrentValue = this.thicknessSetting.currentValue();
                        float fCurrentValue2 = this.lengthSetting.currentValue();
                        float fCurrentValue3 = this.gapSetting.currentValue() + (this.cooldownMultiplierSetting.currentValue() * attackCooldownProgress);
                        float f = fCurrentValue / 2.0f;
                        if (!this.tModeSetting.isValue()) {
                            drawRect(positionMatrix, class154VarDrawEngine, fScreenWidth - f, (fScreenHeight - fCurrentValue3) - fCurrentValue2, fCurrentValue, fCurrentValue2, iComputeColor, iBlack);
                        }
                        drawRect(positionMatrix, class154VarDrawEngine, fScreenWidth - f, fScreenHeight + fCurrentValue3, fCurrentValue, fCurrentValue2, iComputeColor, iBlack);
                        drawRect(positionMatrix, class154VarDrawEngine, (fScreenWidth - fCurrentValue3) - fCurrentValue2, fScreenHeight - f, fCurrentValue2, fCurrentValue, iComputeColor, iBlack);
                        drawRect(positionMatrix, class154VarDrawEngine, fScreenWidth + fCurrentValue3, fScreenHeight - f, fCurrentValue2, fCurrentValue, iComputeColor, iBlack);
                        if (this.centerDotSetting.isValue()) {
                            float fCurrentValue4 = this.centerSizeSetting.currentValue();
                            drawRect(positionMatrix, class154VarDrawEngine, fScreenWidth - (fCurrentValue4 / 2.0f), fScreenHeight - (fCurrentValue4 / 2.0f), fCurrentValue4, fCurrentValue4, iComputeColor, iBlack);
                        }
                        break;
                    case CIRCLE:
                        this.circleProgress = MathUtil.lerp(this.circleProgress, 1.0f - attackCooldownProgress, 4.0f);
                        int iClamp = (int) MathUtil.clamp(this.circleProgress * 360.0f, 0.0f, 360.0f);
                        class154VarDrawEngine.arc(positionMatrix, (int) fScreenWidth, (int) fScreenHeight, this.radiusSetting.currentValue(), 0.0f, 360.0f, this.thicknessSetting.currentValue(), class115VarColorStack.computeColor(30, 30, 30));
                        class154VarDrawEngine.arc(positionMatrix, (int) fScreenWidth, (int) fScreenHeight, this.radiusSetting.currentValue(), 0.0f, iClamp + 6, this.thicknessSetting.currentValue(), iComputeColor);
                        break;
                }
                class154VarDrawEngine.end();
            }
        });
        register(CrosshairRenderEvent.class, class247Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                class247Var.cancel();
            }
        });
    }

    public void drawRect(Matrix4f matrix4f, DrawEngine class154Var, float f, float f2, float f3, float f4, int i, int i2) {
        if (this.outlineSetting.isValue()) {
            class154Var.rectangle(matrix4f, f - 1.0f, f2 - 1.0f, f3 + (1.0f * 2.0f), f4 + (1.0f * 2.0f), i2);
        }
        class154Var.rectangle(matrix4f, f, f2, f3, f4, i);
    }
}
