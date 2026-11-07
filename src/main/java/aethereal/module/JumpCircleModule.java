package aethereal.module;
import aethereal.ui.setting.ColorSetting;
import aethereal.math.Easings;
import aethereal.model.JumpCircleTrailPoint;
import aethereal.event.JumpEvent;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;
import aethereal.type.SettingUnit;
import aethereal.render.ShapeRenderer;
import aethereal.util.StencilBufferUtil;
import aethereal.event.WorldRenderEvent;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

public class JumpCircleModule extends Module {
    static final List<JumpCircleTrailPoint> trailPoints = new ArrayList();
    public final Identifier circleTexture;
    public final Mc mc;

    public final NumberSetting lifetimeSetting;
    public final NumberSetting sizeSetting;
    public final ColorSetting colorSetting;

    public JumpCircleModule() {
        super(ModuleTab.RENDER, "Jump Circle");
        this.circleTexture = Identifier.of("expensive", "textures/circle.png");
        this.mc = Mc.INSTANCE;
        this.lifetimeSetting = new NumberSetting(Lang.PARTICLES_LIFETIME).range(500.0f, 5000.0f).currentValue(1500.0f).step(5.0f).unit(SettingUnit.MILLISECONDS);
        this.sizeSetting = new NumberSetting(Lang.PARTICLES_SIZE).range(0.5f, 3.0f).currentValue(3.0f).unit(SettingUnit.UNITS);
        this.colorSetting = new ColorSetting(Lang.TARGETESP_COLOR);
        addSettings(this.lifetimeSetting, this.sizeSetting, this.colorSetting);
        register(JumpEvent.class, class237Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                addTrailPoint(this.mc.getPlayer());
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (trailPoints.isEmpty()) {
                return;
            }
            Vec3d pos = this.mc.getCamera().getPos();
            float f = 0.0f;
            for (JumpCircleTrailPoint class556Var : trailPoints) {
                float fMethod001 = class556Var.getProgress();
                if (fMethod001 <= 1.0f) {
                    renderCircle(class016Var.matrixStack(), class556Var.position.subtract(pos), this.sizeSetting.currentValue(), 1.0f - fMethod001, (int) f);
                }
                f += 45.0f * (1.0f - fMethod001);
            }
            trailPoints.removeIf(class556Var2 -> {
                return class556Var2.getProgress() > 1.0f;
            });
        });
    }

    public void renderCircle(MatrixStack matrixStack, Vec3d vec3d, double d, float f, int i) {
        double dEase = d * 1.5d * ((double) Easings.EASE_OUT_CUBIC.ease(1.0f - f));
        float f2 = (f > 0.5f ? 1.0f - f : f) * 2.0f;
        float fMin = Math.min((f2 < 0.5f ? 2.0f * f2 * f2 : 1.0f - (((float) Math.pow(((-2.0f) * f2) + 2.0f, 2.0d)) / 2.0f)) * 1.75f, 1.0f);
        matrixStack.push();
        matrixStack.translate(vec3d.getX() - (dEase / 2.0d), vec3d.getY(), vec3d.getZ() - (dEase / 2.0d));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        int color = this.colorSetting.getColor();
        ShapeRenderer.INSTANCE.addTexture(matrixStack.peek().getPositionMatrix(), 0.0f, 0.0f, (float) dEase, (float) dEase, this.circleTexture, (color & 16777215) | (((int) (((color >>> 24) & StencilBufferUtil.STENCIL_MASK) * fMin)) << 24), true, true, true);
        matrixStack.pop();
    }

    public void addTrailPoint(Entity entity) {
        trailPoints.add(new JumpCircleTrailPoint(this, entity.getLerpedPos(Mc.INSTANCE.getTickDelta()).add(0.0d, 0.01d, 0.0d)));
    }
}
