package aethereal.module;
import aethereal.Lang;
import aethereal.annotation.Aliases;
import aethereal.type.SaturationBarStyle;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.io.IOException;
import java.io.InputStream;

@Aliases(aliases = {"Saturation", "Food Bar", "Hunger Bar", "Sat Bar", "Правильная еда"})
public class SaturationBarModule extends Module {
    public final ModeSetting<SaturationBarStyle> style;
    public final ColorSetting color;
    public final ColorSetting gradientTo;
    public final NumberSetting gradientSpeed;
    public final BooleanSetting raiseUnderwater;
    public final NumberSetting pulseSpeed;
    public final NumberSetting pulseMin;
    public final NumberSetting pulseMax;
    public final BooleanSetting pulseWhileFull;

    private static final int SIZE = 9;
    private static int[] fullIcon;
    private static int[] halfIcon;

    private long tickCount;
    private float partialTicks;

    public SaturationBarModule() {
        super(ModuleTab.RENDER, "Saturation Bar");
        this.style = new ModeSetting<SaturationBarStyle>(Lang.SATURATIONBAR_STYLE, Lang.SATURATIONBAR_STYLE_DESC)
                .values(SaturationBarStyle.class)
                .select(SaturationBarStyle.SECOND_ROW);
        this.color = new ColorSetting(Lang.SATURATIONBAR_COLOR, Lang.SATURATIONBAR_COLOR_DESC)
                .setColor(0xFFFFC800)
                .visible(() -> this.style.getValue() != SaturationBarStyle.SECOND_ROW);
        this.gradientTo = new ColorSetting(Lang.SATURATIONBAR_GRADIENT, Lang.SATURATIONBAR_GRADIENT_DESC)
                .setColor(0xFFFF3C38)
                .visible(() -> this.style.getValue() == SaturationBarStyle.GRADIENT);
        this.gradientSpeed = new NumberSetting(Lang.SATURATIONBAR_GRADIENT_SPEED, Lang.SATURATIONBAR_GRADIENT_SPEED_DESC)
                .range(0.25f, 4.0f)
                .step(0.05f)
                .currentValue(1.0f)
                .visible(() -> this.style.getValue() == SaturationBarStyle.GRADIENT);
        this.raiseUnderwater = new BooleanSetting(Lang.SATURATIONBAR_RAISE_UNDERWATER, Lang.SATURATIONBAR_RAISE_UNDERWATER_DESC)
                .setValue(true)
                .visible(() -> this.style.getValue() == SaturationBarStyle.SECOND_ROW || this.style.getValue() == SaturationBarStyle.GRADIENT);
        this.pulseSpeed = new NumberSetting(Lang.SATURATIONBAR_PULSE_SPEED, Lang.SATURATIONBAR_PULSE_SPEED_DESC)
                .range(0.25f, 4.0f)
                .step(0.05f)
                .currentValue(1.25f)
                .visible(() -> this.style.getValue() == SaturationBarStyle.OUTLINE_PULSE);
        this.pulseMin = new NumberSetting(Lang.SATURATIONBAR_PULSE_MIN, Lang.SATURATIONBAR_PULSE_MIN_DESC)
                .range(0.0f, 0.95f)
                .step(0.05f)
                .currentValue(0.4f)
                .visible(() -> this.style.getValue() == SaturationBarStyle.OUTLINE_PULSE);
        this.pulseMax = new NumberSetting(Lang.SATURATIONBAR_PULSE_MAX, Lang.SATURATIONBAR_PULSE_MAX_DESC)
                .range(0.5f, 1.5f)
                .step(0.05f)
                .currentValue(1.0f)
                .visible(() -> this.style.getValue() == SaturationBarStyle.OUTLINE_PULSE);
        this.pulseWhileFull = new BooleanSetting(Lang.SATURATIONBAR_PULSE_WHILE_FULL, Lang.SATURATIONBAR_PULSE_WHILE_FULL_DESC)
                .setValue(true)
                .visible(() -> this.style.getValue() == SaturationBarStyle.OUTLINE_PULSE);
        addSettings(this.style, this.color, this.gradientTo, this.gradientSpeed, this.raiseUnderwater, this.pulseSpeed, this.pulseMin, this.pulseMax, this.pulseWhileFull);
    }

    public void updateTick() {
        this.tickCount++;
    }

    public void updatePartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public void render(DrawContext drawContext, PlayerEntity player, int x, int y) {
        float saturation = player.getHungerManager().getSaturationLevel();
        if (saturation <= 0.0f) return;
        ensureIcons();
        int baseTint = color.getColor() | 0xFF000000;
        int gradToTint = gradientTo.getColor() | 0xFF000000;
        SaturationBarStyle st = style.getValue();
        boolean upperRow = (st == SaturationBarStyle.SECOND_ROW || st == SaturationBarStyle.GRADIENT);
        int rowY = y;
        if (upperRow) {
            rowY = y - 10;
            if (raiseUnderwater.isValue() && (player.isSubmergedIn(FluidTags.WATER) || player.getAir() < player.getMaxAir())) {
                rowY -= 10;
            }
        }
        float basePulse = (st == SaturationBarStyle.OUTLINE_PULSE) ? getPulseMultiplier() : 1.0f;
        float speed = gradientSpeed.currentValue();
        for (int i = 0; i < 10; i++) {
            float s = saturation - (float) i * 2.0f;
            if (s <= 0.0f) break;
            boolean isHalf = s < 2.0f;
            int[] icon = isHalf ? halfIcon : fullIcon;
            int ix = x - i * 8 - 9;
            switch (st) {
                case SECOND_ROW -> drawIcon(drawContext, ix, rowY, icon, 0, 0, Mode.ORIGINAL, 1.0f, 0.0f);
                case OUTLINE -> drawIcon(drawContext, ix, rowY, icon, baseTint, 0, Mode.RECOLOR_OUTLINE, 1.0f, 0.0f);
                case OUTLINE_PULSE -> {
                    float pm = (isHalf || pulseWhileFull.isValue()) ? basePulse : 1.0f;
                    drawIcon(drawContext, ix, rowY, icon, baseTint, 0, Mode.RECOLOR_OUTLINE, pm, 0.0f);
                }
                case GRADIENT -> drawIcon(drawContext, ix, rowY, icon, baseTint, gradToTint, Mode.GRADIENT, 1.0f, speed);
            }
        }
    }

    private enum Mode {
        ORIGINAL, RECOLOR_OUTLINE, GRADIENT
    }

    private float getPulseMultiplier() {
        float speed = pulseSpeed.currentValue();
        float min = pulseMin.currentValue();
        float max = pulseMax.currentValue();
        double time = ((double)tickCount + (double)partialTicks) * 0.05D * (double)speed;
        double sin = Math.sin(time);
        float t = (float)((sin + 1.0D) * 0.5D);
        return MathHelper.lerp(t, min, max);
    }

    private static int lerpColorARGB(int fromARGB, int toARGB, float t) {
        t = MathHelper.clamp(t, 0.0f, 1.0f);
        int a1 = (fromARGB >>> 24) & 0xFF;
        int r1 = (fromARGB >> 16) & 0xFF;
        int g1 = (fromARGB >> 8) & 0xFF;
        int b1 = fromARGB & 0xFF;
        int a2 = (toARGB >>> 24) & 0xFF;
        int r2 = (toARGB >> 16) & 0xFF;
        int g2 = (toARGB >> 8) & 0xFF;
        int b2 = toARGB & 0xFF;
        int ar = MathHelper.lerp(t, a1, a2);
        int rr = MathHelper.lerp(t, r1, r2);
        int gr = MathHelper.lerp(t, g1, g2);
        int br = MathHelper.lerp(t, b1, b2);
        return (ar << 24) | (rr << 16) | (gr << 8) | br;
    }

    private void drawIcon(DrawContext drawContext, int x, int y, int[] icon, int base, int to, Mode mode, float pulseMul, float speed) {
        for (int py = 0; py < SIZE; py++) {
            for (int px = 0; px < SIZE; px++) {
                int argb = icon[py * SIZE + px];
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha == 0) continue;
                int out = switch (mode) {
                    case ORIGINAL -> argb;
                    case RECOLOR_OUTLINE -> isEdge(icon, px, py) ? tintWith(base, pulseMul) : argb;
                    case GRADIENT -> {
                        int grad = gradientColorAt(x + px, base, to, speed);
                        yield (alpha << 24) | (grad & 0x00FFFFFF);
                    }
                };
                drawContext.fill(x + px, y + py, x + px + 1, y + py + 1, out);
            }
        }
    }

    private static boolean isEdge(int[] icon, int px, int py) {
        return isTransparent(icon, px - 1, py) || isTransparent(icon, px + 1, py)
                || isTransparent(icon, px, py - 1) || isTransparent(icon, px, py + 1);
    }

    private static boolean isTransparent(int[] icon, int px, int py) {
        if (px < 0 || py < 0 || px >= SIZE || py >= SIZE) return true;
        return ((icon[py * SIZE + px] >>> 24) & 0xFF) == 0;
    }

    private static int tintWith(int argb, float mul) {
        mul = MathHelper.clamp(mul, 0.0f, 1.5f);
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        int tr = Math.min(255, (int)((float) r * mul));
        int tg = Math.min(255, (int)((float) g * mul));
        int tb = Math.min(255, (int)((float) b * mul));
        return 0xFF000000 | (tr << 16) | (tg << 8) | tb;
    }

    private int gradientColorAt(int screenX, int base, int to, float speed) {
        double phase = (double) screenX / 14.0D + ((double) (tickCount + partialTicks) * 0.04D * (double) speed);
        float t = (float) ((Math.sin(phase) + 1.0D) * 0.5D);
        return lerpColorARGB(base, to, t);
    }

    private static void ensureIcons() {
        if (fullIcon == null) fullIcon = loadSprite("hud/food_full");
        if (halfIcon == null) halfIcon = loadSprite("hud/food_half");
    }

    public static void invalidateIcons() {
        fullIcon = null;
        halfIcon = null;
    }

    private static int[] loadSprite(String name) {
        int[] out = new int[SIZE * SIZE];
        Identifier loc = Identifier.ofVanilla("textures/gui/sprites/" + name + ".png");
        try {
            var res = MinecraftClient.getInstance().getResourceManager().getResource(loc);
            if (res.isEmpty()) return out;
            try (InputStream in = res.get().getInputStream(); NativeImage img = NativeImage.read(in)) {
                int w = img.getWidth();
                int h = img.getHeight();
                for (int y = 0; y < SIZE; y++) {
                    for (int x = 0; x < SIZE; x++) {
                        int sx = x * w / SIZE;
                        int sy = y * h / SIZE;
                        out[y * SIZE + x] = img.getColorArgb(sx, sy);
                    }
                }
            }
        } catch (IOException ignored) {}
        return out;
    }
}
