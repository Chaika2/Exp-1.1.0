package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.event.ModuleStateEvent;
import aethereal.event.WorldRenderEvent;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.NumberSetting;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Aliases(aliases = {"Trail", "Movement Trail", "Player Trail", "Motion Trail"})
public class TrailsModule extends Module {
    private static final float BLINK_SPEED = 0.002f;

    public final List<Point> points;
    public final NumberSetting lengthSetting;
    public final Mc mc;

    public TrailsModule() {
        super(ModuleTab.RENDER, "Trails");
        this.points = new ArrayList<>();
        this.mc = Mc.INSTANCE;
        this.lengthSetting = new NumberSetting(Lang.TRAILS_LENGTH).currentValue(2.0f).range(2.0f, 4.0f).step(0.5f);
        addSettings(this.lengthSetting);
        register(WorldRenderEvent.class, class016Var -> {
            if (!isState() || !this.mc.isWorldLoaded()) {
                return;
            }
            if (this.mc.getGameOptions().getPerspective() == Perspective.FIRST_PERSON) {
                return;
            }
            long currentTime = System.currentTimeMillis();
            long maxAge = (long) (this.lengthSetting.currentValue() * 100L);
            this.points.removeIf(p -> currentTime - p.time > maxAge);
            this.points.add(new Point(this.mc.getPlayer().getLerpedPos(class016Var.renderTickCounter().getTickDelta(false))));
            render3DPoints(class016Var.matrixStack());
        });
        register(ModuleStateEvent.class, class080Var -> {
            if (!class080Var.moduleState()) {
                this.points.clear();
            }
        });
    }

    public void render3DPoints(MatrixStack matrixStack) {
        if (this.points.isEmpty()) {
            return;
        }
        Vec3d cameraPos = this.mc.getCamera().getPos();
        float height = this.mc.getPlayer().getHeight();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        Tessellator tessellator = this.mc.getTesselator();
        MatrixStack.Entry entry = matrixStack.peek();

        BufferBuilder ribbon = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);
        int index = 0;
        for (Point p : this.points) {
            int[] rgb = trailColor();
            float alpha = (float) index / (float) this.points.size() * 0.7f;
            Vec3d pos = p.pos.subtract(cameraPos);
            ribbon.vertex(entry, (float) pos.getX(), (float) (pos.getY() + height), (float) pos.getZ()).color(rgb[0], rgb[1], rgb[2], alpha);
            ribbon.vertex(entry, (float) pos.getX(), (float) pos.getY(), (float) pos.getZ()).color(rgb[0], rgb[1], rgb[2], alpha);
            index++;
        }
        BufferRenderer.drawWithGlobalProgram(ribbon.end());

        RenderSystem.lineWidth(1.0f);
        renderLineStrip(tessellator, entry, cameraPos, height, true);
        renderLineStrip(tessellator, entry, cameraPos, height, false);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public void renderLineStrip(Tessellator tessellator, MatrixStack.Entry entry, Vec3d cameraPos, float height, boolean withHeight) {
        if (this.points.size() < 2) {
            return;
        }
        BufferBuilder line = tessellator.begin(VertexFormat.DrawMode.LINE_STRIP, VertexFormats.POSITION_COLOR);
        int index = 0;
        for (Point p : this.points) {
            int[] rgb = trailColor();
            float alpha = MathHelper.clamp((float) index / (float) this.points.size() * 1.5f, 0.0f, 1.0f);
            Vec3d pos = p.pos.subtract(cameraPos);
            line.vertex(entry, (float) pos.getX(), (float) (pos.getY() + (withHeight ? height : 0.0f)), (float) pos.getZ()).color(rgb[0], rgb[1], rgb[2], alpha);
            index++;
        }
        BufferRenderer.drawWithGlobalProgram(line.end());
    }

    public int[] trailColor() {
        int accent = Expensive.INSTANCE.theme().palette().accent().argb();
        float red = (accent >> 16 & 255) / 255.0f;
        float green = (accent >> 8 & 255) / 255.0f;
        float blue = (accent & 255) / 255.0f;
        float blinkFactor = (float) (Math.sin(System.currentTimeMillis() * BLINK_SPEED) * 0.5d + 0.5d);
        return new int[]{toChannel(MathHelper.lerp(blinkFactor, red, Math.min(red + 0.3f, 1.0f))), toChannel(MathHelper.lerp(blinkFactor, green, Math.min(green + 0.3f, 1.0f))), toChannel(MathHelper.lerp(blinkFactor, blue, Math.min(blue + 0.3f, 1.0f)))};
    }

    private int toChannel(float f) {
        return Math.round(MathHelper.clamp(f, 0.0f, 1.0f) * 255.0f);
    }

    public static class Point {
        public final Vec3d pos;
        public final long time;

        public Point(Vec3d vec3d) {
            this.pos = vec3d;
            this.time = System.currentTimeMillis();
        }
    }
}