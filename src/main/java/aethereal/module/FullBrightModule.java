package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.render.AnimatedFloat;
import aethereal.render.AnimationStack2;
import aethereal.ui.setting.BooleanSetting;
import aethereal.math.DeltaTimeTracker;
import aethereal.math.Easings;
import aethereal.event.GammaEvent;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.event.Render2DEvent;
import aethereal.util.WeightedEngine;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

@Aliases(aliases = {"Full Bright", "Gamma Boost", "Bright Vision", "Night Vision", "No Darkness", "Max Brightness", "Brightness Control", "Enhanced Gamma"})
public class FullBrightModule extends Module {
    public final BooleanSetting dynamicSetting;
    public final NumberSetting brightnessSetting;
    public final NumberSetting minBrightnessSetting;
    public final NumberSetting maxBrightnessSetting;
    public float originalGamma;
    public float smoothedLight;
    public static final float lightFactor = 0.12f;
    public final DeltaTimeTracker deltaTracker;
    public final AnimationStack2 animationStack;
    public final AnimatedFloat gammaAnimation;

    public final Mc mc;
    public boolean gammaStored;

    public FullBrightModule() {
        super(ModuleTab.RENDER, "Full Bright");
        this.dynamicSetting = new BooleanSetting(Lang.FULLBRIGHT_DYNAMIC, Lang.FULLBRIGHT_DYNAMIC_DESC);
        this.brightnessSetting = new NumberSetting(Lang.FULLBRIGHT_BRIGHTNESS).range(0.0f, 10.0f).currentValue(0.5f).step(0.05f).visible(() -> {
            return Boolean.valueOf(!this.dynamicSetting.isValue());
        });
        this.minBrightnessSetting = new NumberSetting(Lang.FULLBRIGHT_MIN_BRIGHTNESS).range(0.0f, 5.0f).currentValue(0.0f).step(0.05f).visible(() -> {
            return Boolean.valueOf(this.dynamicSetting.isValue());
        });
        this.maxBrightnessSetting = new NumberSetting(Lang.FULLBRIGHT_MAX_BRIGHTNESS).range(0.0f, 10.0f).currentValue(10.0f).step(0.05f).visible(() -> {
            return Boolean.valueOf(this.dynamicSetting.isValue());
        });
        this.smoothedLight = 1.0f;
        this.deltaTracker = new DeltaTimeTracker();
        this.animationStack = new AnimationStack2();
        this.gammaAnimation = new AnimatedFloat(250, Easings.LINEAR);
        this.mc = Mc.INSTANCE;
        addSettings(this.brightnessSetting, this.dynamicSetting, this.minBrightnessSetting, this.maxBrightnessSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                World world = player.getWorld();
                BlockPos blockPosOfFloored = BlockPos.ofFloored(player.getX(), player.getEyeY(), player.getZ());
                DimensionType dimension = world.getDimension();
                this.smoothedLight = (this.smoothedLight * 0.88f) + (Math.max(LightmapTextureManager.getBrightness(dimension, world.getLightLevel(LightType.BLOCK, blockPosOfFloored)), LightmapTextureManager.getBrightness(dimension, world.getLightLevel(LightType.SKY, blockPosOfFloored))) * lightFactor);
            }
            if (this.mc.isWorldLoaded() && isState() && !this.gammaStored) {
                this.originalGamma = ((Double) this.mc.getGameOptions().getGamma().getValue()).floatValue();
                this.smoothedLight = 1.0f;
                this.gammaStored = true;
            }
            if (isState()) {
                return;
            }
            this.gammaStored = false;
        });
        register(Render2DEvent.class, class311Var -> {
            if (this.mc.isWorldLoaded()) {
                if (isState() || !this.gammaAnimation.isAtDestination()) {
                    WeightedEngine class141Var = new WeightedEngine(this.deltaTracker.elapsedUnit(), this.animationStack);
                    this.animationStack.begin();
                    this.gammaAnimation.animate(class141Var);
                    this.animationStack.end();
                }
            }
        });
        register(GammaEvent.class, class336Var -> {
            float fMethod004;
            if (this.mc.isWorldLoaded()) {
                if (isState()) {
                    fMethod004 = this.dynamicSetting.isValue() ? computeDynamicGamma(this.smoothedLight) : this.brightnessSetting.currentValue();
                } else {
                    fMethod004 = this.originalGamma;
                }
                this.gammaAnimation.destination(fMethod004);
                if (isState() || !this.gammaAnimation.isAtDestination()) {
                    class336Var.setGamma(this.gammaAnimation.animatedValue());
                    class336Var.cancel();
                }
            }
        });
    }

    public float computeDynamicGamma(float f) {
        float fCurrentValue = this.minBrightnessSetting.currentValue();
        return fCurrentValue + ((this.maxBrightnessSetting.currentValue() - fCurrentValue) * (1.0f - f));
    }
}
