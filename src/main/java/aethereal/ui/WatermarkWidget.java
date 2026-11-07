package aethereal.ui;

import aethereal.ui.setting.BooleanSetting;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.type.HudInfoType;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.ui.setting.OrderedEnumSetting;
import aethereal.ui.setting.SeparatorSetting;
import aethereal.util.ServerUtil;
import aethereal.render.Theme;
import aethereal.render.ThemePalette;
import aethereal.util.WeightedEngine;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BooleanSupplier;
import net.minecraft.client.util.math.MatrixStack;

public class WatermarkWidget extends Draggable {
    public static final String BRAND_NAME = "ZortexDLC";

    float computedWidth;
    float computedHeight;
    final GlTexture iconTexture;
    final GlTexture backgroundTexture;
    final MsdfFont brandFont;
    final MsdfFont infoFont;

    public final OrderedEnumSetting<HudInfoType> hudLines;
    public final NumberSetting scale;
    public final BooleanSetting use12hFormat;
    public final ColorSetting color;

    public static final WatermarkStar[] stars = {
            new WatermarkStar(19.5f, 0.0f, 6.0f, 6.0f, 0.1f),
            new WatermarkStar(9.5f, 0.0f, 6.0f, 6.0f, 0.1f),
            new WatermarkStar(3.0f, 5.0f, 6.0f, 6.0f, 0.1f),
            new WatermarkStar(3.5f, -3.0f, 6.0f, 6.0f, 0.1f),
            new WatermarkStar(6.0f, 14.0f, 6.0f, 6.0f, 0.1f),
            new WatermarkStar(10.5f, 20.0f, 6.0f, 6.0f, 0.1f),
            new WatermarkStar(19.5f, 16.0f, 7.0f, 7.0f, 0.1f),
            new WatermarkStar(24.5f, 12.0f, 7.0f, 7.0f, 0.1f)
    };

    public WatermarkWidget(BooleanSupplier booleanSupplier) {
        super("Watermark", booleanSupplier);

        this.iconTexture = new GlTexture(new ClasspathResource("/icons/hud/cat.gif"));
        this.backgroundTexture = new GlTexture(new ClasspathResource("/textures/hud_background.png"));

        this.brandFont = Fonts.INTER_EXTRA_BOLD.get();
        this.infoFont = Fonts.INTER_BOLD.get();
        this.hudLines = new OrderedEnumSetting(Lang.WIDGET_WATERMARK_HUD_LINES)
                .values(HudInfoType.class)
                .ordered(HudInfoType.LATENCY, HudInfoType.FRAMERATE, HudInfoType.CURRENT_TIME, HudInfoType.SERVER_NAME)
                .selected(HudInfoType.LATENCY, HudInfoType.FRAMERATE);
        this.scale = new NumberSetting(Lang.WIDGET_WATERMARK_SCALE)
                .currentValue(1.0f)
                .range(0.5f, 2.0f)
                .step(0.05f);
        this.use12hFormat = new BooleanSetting(Lang.WIDGET_WATERMARK_USE_12H_FORMAT)
                .visible(() -> Boolean.valueOf(this.hudLines.isSelected(HudInfoType.CURRENT_TIME)));
        this.color = new ColorSetting(Lang.WIDGET_WATERMARK_COLOR).value(0xA56CFF);

        addSettings(this.color, this.scale, this.use12hFormat, new SeparatorSetting(), this.hudLines);
        this.x = 10.0f;
        this.y = 10.0f;
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        String strMethod005;
        if (isVisible()) {
            Mc class815Var = Mc.INSTANCE;
            String strMainText = mainText(class815Var);
            float fMainWidth = this.infoFont.getWidth(strMainText, 10.0f);
            float width = 6.0f + 26.0f + 8.0f + fMainWidth + 6.0f;

            for (HudInfoType e : this.hudLines.orderedValues()) {
                if (this.hudLines.isSelected(e) && (strMethod005 = textFor(e, class815Var)) != null && !strMethod005.isEmpty()) {
                    width += 10.0f + this.infoFont.getWidth(" | " + strMethod005, 10.0f);
                }
            }
            this.computedWidth = width;
            this.computedHeight = 26.0f;
        }
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (isVisible()) {
            Mc class815Var = Mc.INSTANCE;
            DrawEngine class154VarDrawEngine = class809Var.drawEngine();
            ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
            MatrixStack matrixStack = class809Var.matrixStack();
            Theme class760VarTheme = class809Var.theme();
            ThemePalette class764VarPalette = class760VarTheme.palette();
            float fCurrentValue = this.scale.currentValue();

            matrixStack.push();
            matrixStack.translate(this.x, this.y, 0.0f);
            matrixStack.scale(fCurrentValue, fCurrentValue, 1.0f);

            float f = this.computedHeight / 2.0f;
            int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
            int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());

            class154VarDrawEngine.roundedRectangle(
                    matrixStack.peek().getPositionMatrix(),
                    0.0f, 0.0f, this.computedWidth, this.computedHeight,
                    9.0f, 2.5f,
                    class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()),
                    iComputeColor2, iComputeColor2, iComputeColor, iComputeColor
            );

            class154VarDrawEngine.beginStencil();
            class154VarDrawEngine.roundedRectangle(
                    matrixStack.peek().getPositionMatrix(),
                    0.0f, 2.0f, this.computedWidth, this.computedHeight - 4.0f,
                    9.0f, 2.5f,
                    class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()),
                    iComputeColor2, iComputeColor2, iComputeColor, iComputeColor
            );
            class154VarDrawEngine.prepareStencil(1);
            class154VarDrawEngine.texture(
                    matrixStack.peek().getPositionMatrix(),
                    0.0f, 0.0f, this.computedWidth, this.computedHeight,
                    class154VarDrawEngine.bindTexture(this.backgroundTexture.textureWithSTB()),
                    class115VarColorStack.white()
            );

            float fIconX = 6.0f;
            float fIconY = f - 13.0f;
            class154VarDrawEngine.roundedRectangle(
                    matrixStack.peek().getPositionMatrix(),
                    fIconX, fIconY, 26.0f, 26.0f,
                    8.0f,
                    class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb())
            );

            renderIcon(class154VarDrawEngine, matrixStack, fIconX, fIconY, class115VarColorStack, class764VarPalette);

            float fTextX = fIconX + 26.0f + 8.0f;
            int iColor = class115VarColorStack.computeColor(this.color.getColor());
            int iColorEnd = class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb());
            float fTextY = f - (this.infoFont.getHeight(10.0f) / 2.0f);

            String strMainText = mainText(class815Var);
            float width = fTextX;
            class154VarDrawEngine.msdfFontWithHorizontalGradient(
                    matrixStack.peek().getPositionMatrix(),
                    this.infoFont, strMainText, width, fTextY, 10.0f, 0.05f, iColor, iColorEnd
            );
            width += this.infoFont.getWidth(strMainText, 10.0f);

            for (HudInfoType e : this.hudLines.orderedValues()) {
                String strMethod005 = textFor(e, class815Var);
                if (this.hudLines.isSelected(e) && strMethod005 != null && !strMethod005.isEmpty()) {
                    String strLine = " | " + strMethod005;
                    width += 10.0f;
                    class154VarDrawEngine.msdfFontWithHorizontalGradient(
                            matrixStack.peek().getPositionMatrix(),
                            this.infoFont, strLine, width, fTextY, 10.0f, 0.05f, iColor, iColorEnd
                    );
                    width += this.infoFont.getWidth(strLine, 10.0f);
                }
            }

            class154VarDrawEngine.endStencil();
            matrixStack.pop();
        }
    }

    private void renderIcon(DrawEngine drawEngine, MatrixStack matrixStack, float x, float y, ColorStack colorStack, ThemePalette palette) {
        if (this.iconTexture == null) {
            return;
        }
        int iBindTexture;
        try {
            iBindTexture = drawEngine.bindTexture(this.iconTexture.textureWithSTB());
        } catch (Throwable ignore) {
            return;
        }
        float size = 22.0f;
        drawEngine.texture(
                matrixStack.peek().getPositionMatrix(),
                x + ((26.0f - size) / 2.0f), y + ((26.0f - size) / 2.0f), size, size,
                iBindTexture,
                colorStack.white()
        );
    }

    public String nickname() {
        String strUsername = Mc.INSTANCE.getSession() != null ? Mc.INSTANCE.getSession().getUsername() : null;
        return strUsername == null || strUsername.isBlank() ? "Player" : strUsername;
    }

    public String mainText(Mc class815Var) {
        return BRAND_NAME + " | " + nickname();
    }

    @Override
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        return false;
    }

    @Override
    public boolean cursor(MouseMoveInput class808Var, boolean z) {
        return z && !class808Var.intercepted();
    }

    @Override
    public void animate(WeightedEngine class141Var) {
    }

    @Override
    public void update() {
    }

    public String textFor(HudInfoType class656Var, Mc class815Var) {
        switch (class656Var.ordinal()) {
            case 1:
                return getPing() + "ms";
            case 2:
                return class815Var.getCurrentFps() + " FPS";
            case 3:
                return formatTime();
            case 4:
                return serverAddress();
            default:
                return nickname();
        }
    }

    public String formatTime() {
        return LocalTime.now().format(this.use12hFormat.isValue() ? DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH) : DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String serverAddress() {
        String serverIp = ServerUtil.getServerIp();
        return serverIp == null ? "" : serverIp.toLowerCase();
    }

    public int getPing() {
        try {
            if (Mc.INSTANCE.isWorldLoaded() && Mc.INSTANCE.getNetworkHandler() != null && Mc.INSTANCE.getPlayer() != null) {
                return Mc.INSTANCE.getNetworkHandler().getPlayerList().stream().filter(playerListEntry -> {
                    return playerListEntry.getProfile().getId().equals(Mc.INSTANCE.getPlayer().getUuid());
                }).findFirst().map((v0) -> {
                    return v0.getLatency();
                }).orElse(0);
            }
        } catch (Exception ignore) {
        }
        return 0;
    }

    @Override
    public float width() {
        return this.computedWidth * this.scale.currentValue();
    }

    @Override
    public float height() {
        return this.computedHeight * this.scale.currentValue();
    }
}
