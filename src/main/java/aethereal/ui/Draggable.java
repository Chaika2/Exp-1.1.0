package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.ScreenResolution;
import aethereal.ui.setting.Setting;
import aethereal.util.SnapManager;
import aethereal.model.SnapResult;
import aethereal.util.StencilBufferUtil;
import aethereal.math.Stopwatch;
import aethereal.module.ThemesModule;
import aethereal.render.BlurEffect;
import aethereal.render.ThemeManager;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public abstract class Draggable {
    public final String name;
    public float x;
    public float y;
    public float aC;
    public float L;
    public BooleanSupplier visibility;
    public boolean dragging;
    public boolean clicked;
    public boolean cursorHovered;
    public SnapManager snapGrid;
    public List<Draggable> siblingWidgets;

    public ScreenResolution resolution;
    public final List<Setting> settings = new ArrayList();

    public ToggleAnimator clickAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public ToggleAnimator hoveredAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public ToggleAnimator tooltipAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public boolean excludeFromSnapGrid = false;

    public final Stopwatch lastResizeTimer = new Stopwatch(false);

    public Draggable(String str, BooleanSupplier booleanSupplier) {
        this.name = str;
        this.visibility = booleanSupplier;
    }

    public void addSettings(Setting... class661VarArr) {
        this.settings.addAll(Arrays.asList(class661VarArr));
    }

    public void draw(DragRenderContext class809Var) {
        if (this.resolution == null || this.resolution.screenWidth() <= 0 || this.resolution.screenHeight() <= 0) {
            this.resolution = ScreenResolution.resolution();
        } else if (isVisible()) {
            drawHudBackdrop(class809Var);
            render(class809Var);
        }
    }

    protected void drawHudBackdrop(DragRenderContext context) {
        ThemesModule module = ThemesModule.instance();
        if (module == null) {
            return;
        }
        boolean blurEnabled = ThemesModule.hudBlurActive();
        boolean glowEnabled = ThemesModule.hudGlowActive();
        if (!blurEnabled && !glowEnabled) {
            return;
        }
        float widgetWidth = width();
        float widgetHeight = height();
        if (widgetWidth <= 0.0f || widgetHeight <= 0.0f) {
            return;
        }
        DrawEngine engine = context.drawEngine();
        Matrix4f matrix4f = context.matrixStack().peek().getPositionMatrix();
        ColorStack colorStack = engine.colorStack();
        float radius = hudCornerRadius();
        if (glowEnabled) {
            drawHudGlow(engine, matrix4f, colorStack, module, widgetWidth, widgetHeight, radius);
        }
        if (blurEnabled) {
            BlurEffect blur = Expensive.INSTANCE.hudBlur();
            if (blur != null && blur.getBlurFramebuffer() != null) {
                engine.roundedBlur(matrix4f, this.x, this.y, widgetWidth, widgetHeight, radius, 2.0f, colorStack.computeColor(ThemePalette.white.argb(), 0.4f), blur.getBlurFramebuffer().getColorAttachment());
            }
        }
    }

    protected void drawHudGlow(DrawEngine engine, Matrix4f matrix4f, ColorStack colorStack, ThemesModule module, float widgetWidth, float widgetHeight, float radius) {
        float strength = module.hudGlowStrength.currentValue() / 100.0f;
        float opacity = module.hudGlowOpacity.currentValue() / 100.0f;
        int accent = ThemeManager.INSTANCE.globalColor((float) (System.nanoTime() / 1000000L));
        ColorStack s = colorStack;
        for (int i = 5; i >= 1; i--) {
            float spread = i * (1.5f + (2.5f * strength));
            float alpha = opacity * (1.0f - (i / 5.0f)) * 0.45f;
            engine.roundedRectangle(matrix4f, this.x - spread, this.y - spread, widgetWidth + (spread * 2.0f), widgetHeight + (spread * 2.0f), Math.max(1.0f, radius + spread), Math.max(1.0f, spread), s.computeColor(accent, alpha), s.computeColor(accent, 0.0f), s.computeColor(accent, 0.0f), s.computeColor(accent, 0.0f), s.computeColor(accent, 0.0f));
        }
    }

    public float hudCornerRadius() {
        return 10.0f;
    }

    public void handleResize(int i, int i2) {
        this.resolution = new ScreenResolution(i, i2);
        this.lastResizeTimer.reset();
    }

    public void applySavedPosition(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public void updateDrag(DragRenderContext class809Var) {
        ScreenResolution class710VarResolution;
        Mouse mouse = Mc.INSTANCE.getMouse();
        Screen currentScreen = Mc.INSTANCE.getCurrentScreen();
        MenuWindow class776VarMenuWindow = Expensive.INSTANCE.menuWindow();
        if (class776VarMenuWindow != null && class776VarMenuWindow.visible()) {
            WidgetSettingsPopup class810Var = (WidgetSettingsPopup) Expensive.INSTANCE.windowController().getWindow(WidgetSettingsPopup.class);
            if (class810Var != null) {
                class810Var.closeWindow();
                return;
            }
            return;
        }
        if (!(currentScreen instanceof ChatScreen)) {
            this.dragging = false;
            this.clicked = false;
            if (this.snapGrid != null) {
                this.snapGrid.clear();
            }
            WidgetSettingsPopup class810Var2 = (WidgetSettingsPopup) Expensive.INSTANCE.windowController().getWindow(WidgetSettingsPopup.class);
            if (class810Var2 != null) {
                class810Var2.close();
                return;
            }
            return;
        }
        if (!this.dragging || (class710VarResolution = class809Var.resolution()) == null || class710VarResolution.screenWidth() <= 0 || class710VarResolution.screenHeight() <= 0) {
            return;
        }
        float fScaleFactor = class809Var.scaleFactor();
        float x = (float) (mouse.getX() / ((double) fScaleFactor));
        float y = (float) (mouse.getY() / ((double) fScaleFactor));
        float f = x - this.aC;
        float f2 = y - this.L;
        if (this.snapGrid == null || this.siblingWidgets == null || isExcludeFromSnapGrid()) {
            this.x = f;
            this.y = f2;
        } else {
            SnapResult class813VarCalculateSnap = this.snapGrid.calculateSnap(this, this.siblingWidgets, f, f2, class710VarResolution.screenWidth(), class710VarResolution.screenHeight());
            this.x = class813VarCalculateSnap.x;
            this.y = class813VarCalculateSnap.y;
        }
        boolean z = this.x < 0.0f || this.y < 0.0f || this.x + width() > ((float) class710VarResolution.screenWidth()) || this.y + height() > ((float) class710VarResolution.screenHeight());
        if (class710VarResolution.isExceeding(width(), height()) && z) {
            this.x = MathUtil.clamp(this.x, 0.0f, class710VarResolution.screenWidth() - width());
            this.y = MathUtil.clamp(this.y, 0.0f, class710VarResolution.screenHeight() - height());
        }
    }

    public void animation(WeightedEngine class141Var) {
        this.hoveredAnimation.state(this.cursorHovered).animate(class141Var);
        this.tooltipAnimation.state(this.cursorHovered && (Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen)).animate(class141Var);
        this.clickAnimation.state(this.clicked).animate(class141Var);
        animate(class141Var);
    }

    public void updateWidget() {
        if (isVisible()) {
            update();
        }
    }

    public boolean onCursor(MouseMoveInput class808Var) {
        boolean zIsWithinBounds = class808Var.isWithinBounds(this.x, this.y, width(), height());
        boolean zCursor = cursor(class808Var, zIsWithinBounds);
        if (!isVisible()) {
            this.cursorHovered = false;
            return false;
        }
        if (class808Var.intercepted()) {
            this.cursorHovered = false;
        } else {
            this.cursorHovered = zIsWithinBounds;
            if (zIsWithinBounds) {
                return true;
            }
        }
        return zCursor;
    }

    public boolean onClick(MouseButtonInput2 class807Var) {
        if (!isVisible()) {
            return false;
        }
        boolean zIsWithinBounds = class807Var.isWithinBounds(this.x, this.y, width(), height());
        boolean zClick = click(class807Var, zIsWithinBounds);
        if (class807Var.isLeftButtonPressed()) {
            if (!class807Var.intercepted() && class807Var.press() && zIsWithinBounds) {
                if (zClick) {
                    return true;
                }
                this.clicked = true;
                float fMouseX = class807Var.mouseX();
                float fMouseY = class807Var.mouseY();
                this.aC = fMouseX - this.x;
                this.L = fMouseY - this.y;
                this.dragging = true;
                return true;
            }
            if (class807Var.release()) {
                this.clicked = false;
                this.dragging = false;
                if (this.snapGrid == null) {
                    return true;
                }
                this.snapGrid.clear();
                return true;
            }
        }
        if (!class807Var.isRightButtonPressed() || class807Var.intercepted() || !class807Var.press() || !zIsWithinBounds || zClick || this.dragging) {
            return false;
        }
        WidgetSettingsPopup class810Var = (WidgetSettingsPopup) Expensive.INSTANCE.windowController().getWindow(WidgetSettingsPopup.class);
        if (this.settings.isEmpty()) {
            if (class810Var == null) {
                return true;
            }
            class810Var.close();
            return true;
        }
        if (class810Var != null) {
            if (class810Var.matchesWidget(this.name) && class810Var.isOpen()) {
                class810Var.closeWindow();
                return true;
            }
            class810Var.close();
            Expensive.INSTANCE.windowController().removeWindow(class810Var);
        }
        WidgetSettingsPopup class810Var2 = new WidgetSettingsPopup(this.name, this.settings);
        Expensive.INSTANCE.windowController().newWindow(class810Var2);
        class810Var2.openAt(this.x, this.y, width(), height(), class807Var.resolution(), class807Var.scaleFactor());
        return true;
    }

    public void drawTooltip(DragRenderContext class809Var) {
        float fSmoothAnimation = this.tooltipAnimation.smoothAnimation();
        if (fSmoothAnimation <= 0.0f) {
            return;
        }
        DrawEngine class154VarDrawEngine = class809Var.drawEngine();
        MatrixStack matrixStack = class809Var.matrixStack();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        ArrayList arrayList = new ArrayList();
        arrayList.add(Lang.WIDGET_DRAG_HINT.effective());
        if (!this.settings.isEmpty()) {
            arrayList.add(Lang.WIDGET_SETTINGS_HINT.effective());
        }
        float height = Fonts.INTER_SEMIBOLD.get().getHeight(12.0f);
        float size = (this.y - (height * arrayList.size())) - 4.0f;
        boolean z = size < 0.0f;
        if (z) {
            size = this.y + height() + 4.0f;
        }
        float f = (z ? -4 : 4) * (1.0f - fSmoothAnimation);
        for (int i = 0; i < arrayList.size(); i++) {
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_SEMIBOLD.get(), (String) arrayList.get(i), ((int) this.x) + 3, size + (i * height) + f, 12.0f, 0.0f, class115VarColorStack.computeColor(fSmoothAnimation, StencilBufferUtil.STENCIL_MASK, StencilBufferUtil.STENCIL_MASK, StencilBufferUtil.STENCIL_MASK));
        }
    }

    public boolean hasTooltip() {
        return true;
    }

    public abstract void layout(DragRenderContext class809Var);

    public abstract void render(DragRenderContext class809Var);

    public abstract boolean click(MouseButtonInput2 class807Var, boolean z);

    public abstract boolean cursor(MouseMoveInput class808Var, boolean z);

    public abstract void animate(WeightedEngine class141Var);

    public abstract void update();

    public abstract float width();

    public abstract float height();

    public void resetCursor() {
        this.cursorHovered = false;
    }

    public boolean isVisible() {
        return this.visibility.getAsBoolean();
    }

    public String getName() {
        return this.name;
    }

    public List<Setting> getSettings() {
        return this.settings;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getDragX() {
        return this.aC;
    }

    public float getDragY() {
        return this.L;
    }

    public BooleanSupplier getVisibility() {
        return this.visibility;
    }

    public boolean isDragging() {
        return this.dragging;
    }

    public ToggleAnimator getClickAnimation() {
        return this.clickAnimation;
    }

    public ToggleAnimator getHoveredAnimation() {
        return this.hoveredAnimation;
    }

    public ToggleAnimator getTooltipAnimation() {
        return this.tooltipAnimation;
    }

    public boolean isClicked() {
        return this.clicked;
    }

    public boolean isCursorHovered() {
        return this.cursorHovered;
    }

    public SnapManager getSnapGrid() {
        return this.snapGrid;
    }

    public List<Draggable> getSiblingWidgets() {
        return this.siblingWidgets;
    }

    public boolean isExcludeFromSnapGrid() {
        return this.excludeFromSnapGrid;
    }

    public ScreenResolution getResolution() {
        return this.resolution;
    }

    public Stopwatch getLastResizeTimer() {
        return this.lastResizeTimer;
    }

    public void setX(float f) {
        this.x = f;
    }

    public void setY(float f) {
        this.y = f;
    }

    public void setDragX(float f) {
        this.aC = f;
    }

    public void setDragY(float f) {
        this.L = f;
    }

    public void setSnapGrid(SnapManager class811Var) {
        this.snapGrid = class811Var;
    }

    public void setSiblingWidgets(List<Draggable> list) {
        this.siblingWidgets = list;
    }

    public void setExcludeFromSnapGrid(boolean z) {
        this.excludeFromSnapGrid = z;
    }
}
