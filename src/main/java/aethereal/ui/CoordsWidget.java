package aethereal.ui;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.type.NotificationType;
import aethereal.render.ScreenResolution;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class CoordsWidget extends Draggable {
    static final float widgetHeight = 26.0f;
    static final float iconPadding = 4.0f;
    static final float iconBackgroundSize = 18.0f;
    static final int iconSize = 10;
    static final float groupSpacing = 6.0f;
    static final float columnSpacing = 6.0f;
    static final float cornerRadius = 8.0f;
    static final float copyButtonHeight = 16.0f;
    static final float buttonCornerRadius = 6.0f;
    static final float buttonInnerPadding = 4.0f;
    static final float copyTextGap = 3.0f;
    static final float labelFontSize = 9.0f;
    static final float valueFontSize = 11.0f;
    static final float defaultMargin = 10.0f;
    static final long copiedDurationMs = 700;
    static final String[] axisLabels = {"X", "Y", "Z"};
    static final String speedLabel = "BPS";
    public final GlTexture earthIcon;
    public final GlTexture backgroundTexture;
    public final GlTexture copyIcon;
    public final GlTexture checkmarkIcon;
    public final MsdfFont coordFont;

    public final MsdfFont buttonFont;
    public final ToggleAnimator expandAnimator;
    public final ToggleAnimator buttonHoverAnimator;
    public final ToggleAnimator chatSlideAnimator;

    public final ToggleAnimator copiedAnimator;

    public final WidgetBounds copyButtonBounds;
    public float width;
    public float height;
    public long copiedUntil;

    public CoordsWidget(BooleanSupplier booleanSupplier) {
        super("Coords", booleanSupplier);
        this.earthIcon = new GlTexture(new ClasspathResource("/icons/menu/new/earth.png"));
        this.backgroundTexture = new GlTexture(new ClasspathResource("/textures/hud_background.png"));
        this.copyIcon = new GlTexture(new ClasspathResource("/icons/menu/new/copy.png"));
        this.checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
        this.coordFont = Fonts.INTER_EXTRA_BOLD.get();
        this.buttonFont = Fonts.INTER_BOLD.get();
        this.expandAnimator = ToggleAnimator.times(2, 90, Easings.LINEAR);
        this.buttonHoverAnimator = new ToggleAnimator(180, Easings.LINEAR);
        this.chatSlideAnimator = new ToggleAnimator(220, Easings.EASE_OUT_CUBIC);
        this.copiedAnimator = new ToggleAnimator(220, Easings.EASE_OUT_CUBIC);
        this.copyButtonBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.x = defaultMargin;
        this.y = defaultMargin;
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        ScreenResolution class710VarResolution;
        if (isVisible() && (class710VarResolution = class809Var.resolution()) != null) {
            int[] iArrMethod001 = getPlayerCoords();
            float width = 28.0f;
            for (int i = 0; i < axisLabels.length; i++) {
                width = width + this.coordFont.getWidth(axisLabels[i], labelFontSize) + this.coordFont.getWidth(" ", valueFontSize) + this.coordFont.getWidth(String.valueOf(iArrMethod001[i]), valueFontSize);
                if (i != axisLabels.length - 1) {
                    width += 6.0f;
                }
            }
            width += 6.0f + this.coordFont.getWidth(speedLabel, labelFontSize) + this.coordFont.getWidth(" ", valueFontSize) + this.coordFont.getWidth(getSpeedBps(), valueFontSize);
            float width2 = this.buttonFont.getWidth(Lang.WIDGET_COORDS_COPY.effective(), defaultMargin);
            float width3 = 21.0f + width2 + ((this.buttonFont.getWidth(Lang.WIDGET_COORDS_COPIED.effective(), defaultMargin) - width2) * this.copiedAnimator.smoothAnimation());
            this.copyButtonBounds.withSize(width3, copyButtonHeight);
            this.width = width + ((6.0f + width3) * Math.min(this.expandAnimator.smoothAnimation(), 1.0f)) + cornerRadius;
            this.height = widgetHeight;
            float fScaleFactor = class809Var.scaleFactor();
            float scaleFactor = (float) ((12.0d * Mc.INSTANCE.getWindow().getScaleFactor()) / ((double) fScaleFactor));
            float f = 5.0f / fScaleFactor;
            float fScreenHeight = (class710VarResolution.screenHeight() - height()) - 7.0f;
            this.x = f;
            this.y = fScreenHeight - (scaleFactor * this.chatSlideAnimator.smoothAnimation());
        }
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (isVisible()) {
            DrawEngine class154VarDrawEngine = class809Var.drawEngine();
            MatrixStack matrixStack = class809Var.matrixStack();
            ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
            ThemePalette class764VarPalette = class809Var.theme().palette();
            float fHeight = this.y + (height() / 2.0f);
            int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
            int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());
            class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, width(), height(), cornerRadius, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iComputeColor2, iComputeColor2, iComputeColor, iComputeColor);
            class154VarDrawEngine.texture(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, class154VarDrawEngine.bindTexture(this.backgroundTexture.textureWithSTB()), class115VarColorStack.white());
            class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x + 4.0f, fHeight - labelFontSize, iconBackgroundSize, iconBackgroundSize, 6.0f, class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 0.1f));
            class154VarDrawEngine.textureVerticalCHorizontalC(matrixStack.peek().getPositionMatrix(), this.earthIcon, this.x + 4.0f + labelFontSize, fHeight, iconSize, iconSize, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
            int[] iArrMethod001 = getPlayerCoords();
            float width = this.x + 4.0f + iconBackgroundSize + 6.0f;
            int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(700).argb());
            int iComputeColor4 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
            for (int i = 0; i < axisLabels.length; i++) {
                String str = axisLabels[i];
                String strValueOf = String.valueOf(iArrMethod001[i]);
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.coordFont, str, width, fHeight - (this.coordFont.getHeight(labelFontSize) / 2.0f), labelFontSize, 0.05f, iComputeColor3);
                float width2 = width + this.coordFont.getWidth(str, labelFontSize) + this.coordFont.getWidth(" ", valueFontSize);
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.coordFont, strValueOf, width2, fHeight - (this.coordFont.getHeight(valueFontSize) / 2.0f), valueFontSize, 0.05f, iComputeColor4);
                width = width2 + this.coordFont.getWidth(strValueOf, valueFontSize);
                if (i != axisLabels.length - 1) {
                    width += 6.0f;
                }
            }
            String speedText = getSpeedBps();
            float speedX = width + 6.0f;
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.coordFont, speedLabel, speedX, fHeight - (this.coordFont.getHeight(labelFontSize) / 2.0f), labelFontSize, 0.05f, iComputeColor3);
            float speedValueX = speedX + this.coordFont.getWidth(speedLabel, labelFontSize) + this.coordFont.getWidth(" ", valueFontSize);
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.coordFont, speedText, speedValueX, fHeight - (this.coordFont.getHeight(valueFontSize) / 2.0f), valueFontSize, 0.05f, iComputeColor4);
            width = speedValueX + this.coordFont.getWidth(speedText, valueFontSize);
            this.copyButtonBounds.withPosition(width + 6.0f, fHeight - (this.copyButtonBounds.height() / 2.0f));
            float fMax = Math.max(this.expandAnimator.smoothAnimation() - 1.0f, 0.0f);
            float fSmoothAnimation = this.copiedAnimator.smoothAnimation();
            float f = 1.0f - fSmoothAnimation;
            class115VarColorStack.push();
            class115VarColorStack.alpha(fMax);
            class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.copyButtonBounds.x(), this.copyButtonBounds.y(), this.copyButtonBounds.width(), this.copyButtonBounds.height(), 6.0f, class115VarColorStack.computeColor(11843002, 0.1f + (0.1f * this.buttonHoverAnimator.smoothAnimation())));
            float fX = this.copyButtonBounds.x() + 4.0f;
            float fY = this.copyButtonBounds.y() + (this.copyButtonBounds.height() / 2.0f);
            float f2 = fX + defaultMargin + copyTextGap;
            float fY2 = (this.copyButtonBounds.y() + (this.copyButtonBounds.height() / 2.0f)) - (this.buttonFont.getHeight(defaultMargin) / 2.0f);
            class115VarColorStack.push();
            class115VarColorStack.alpha(f * fMax);
            class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.copyIcon, fX, fY, iconSize, iconSize, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.buttonFont, Lang.WIDGET_COORDS_COPY.effective(), f2, fY2, defaultMargin, 0.05f, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
            class115VarColorStack.pop();
            class115VarColorStack.push();
            class115VarColorStack.alpha(fSmoothAnimation * fMax);
            class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.checkmarkIcon, fX, fY, iconSize, iconSize, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.buttonFont, Lang.WIDGET_COORDS_COPIED.effective(), f2, fY2, defaultMargin, 0.05f, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
            class115VarColorStack.pop();
            class115VarColorStack.pop();
        }
        if (Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen) {
            return;
        }
        this.expandAnimator.state(false);
        this.buttonHoverAnimator.state(false);
    }

    @Override
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        boolean zIsWithinBounds = class807Var.isWithinBounds(this.copyButtonBounds.x(), this.copyButtonBounds.y(), this.copyButtonBounds.width(), this.copyButtonBounds.height());
        if (class807Var.button() == 0 && class807Var.press() && z && !class807Var.intercepted() && !zIsWithinBounds) {
            return true;
        }
        if (class807Var.button() != 0 || !class807Var.press() || !z || !zIsWithinBounds || class807Var.intercepted()) {
            return false;
        }
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return true;
        }
        StringUtil.copyToClipboard(((int) player.getX()) + ", " + ((int) player.getY()) + ", " + ((int) player.getZ()));
        this.copiedUntil = System.currentTimeMillis() + copiedDurationMs;
        Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.WIDGET_COORDS_COPIED_IN_CLIPBOARD.effective()), 3L, TimeUnit.SECONDS);
        return true;
    }

    @Override
    public boolean cursor(MouseMoveInput class808Var, boolean z) {
        boolean zIsWithinBounds = class808Var.isWithinBounds(this.copyButtonBounds.x(), this.copyButtonBounds.y(), this.copyButtonBounds.width(), this.copyButtonBounds.height());
        boolean z2 = z && !class808Var.intercepted();
        this.expandAnimator.state(z2);
        boolean z3 = zIsWithinBounds && this.expandAnimator.state() && !class808Var.intercepted();
        this.buttonHoverAnimator.state(z3);
        return z2 || z3;
    }

    @Override
    public void animate(WeightedEngine class141Var) {
        this.expandAnimator.animate(class141Var);
        this.buttonHoverAnimator.animate(class141Var);
        this.copiedAnimator.state(System.currentTimeMillis() < this.copiedUntil).animate(class141Var);
        this.chatSlideAnimator.state(Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen).animate(class141Var);
    }

    @Override
    public void update() {
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return this.height;
    }

    @Override
    public boolean hasTooltip() {
        return false;
    }

    public static int[] getPlayerCoords() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return player == null ? new int[]{0, 0, 0} : new int[]{(int) player.getX(), (int) player.getY(), (int) player.getZ()};
    }

    public static String getSpeedBps() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return "0.0";
        }
        Vec3d velocity = player.getVelocity();
        double blocksPerSecond = Math.hypot(velocity.getX(), velocity.getZ()) * 20.0d;
        return String.format(Locale.ROOT, "%.1f", blocksPerSecond);
    }
}
