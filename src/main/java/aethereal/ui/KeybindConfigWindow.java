package aethereal.ui;
import aethereal.type.BindMode;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.ui.setting.KeybindSettingElement;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.util.RenderCommandQueue;
import aethereal.model.ScrollInput;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KeybindConfigWindow extends OverlayWidget {
    public final FrameElementColumn contentColumn;
    public final Translation title;
    public final String labelText;
    public final float headerHeight;
    public final KeybindSettingElement keybindElement;

    public final DualToggleElement bindModeToggle;
    public Supplier<BindMode> bindModeSupplier;
    public final MsdfFont font = Fonts.INTER_SEMIBOLD.get();
    public final GlTexture propertyIcon = new GlTexture(new ClasspathResource("/icons/menu/new/property.png"));
    public final GlTexture keyboardIcon = new GlTexture(new ClasspathResource("/icons/menu/new/keyboard.png"));
    public final ToggleAnimator openAnimation = new ToggleAnimator(Easings.EASE_IN_OUT_CUBIC);
    public final WidgetBounds labelBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public final ToggleAnimator labelHoverAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public final IconLabelBadge keybindBadge = new IconLabelBadge(this.propertyIcon, Translation.clearText("KEYBIND"));

    public KeybindConfigWindow(Translation class254Var, String str, Supplier<List<Integer>> supplier) {
        setSize(241.0f, 48.0f);
        this.title = class254Var;
        this.labelText = str;
        this.contentColumn = new FrameElementColumn(width() - 28.0f, 14.0f);
        this.keybindElement = new KeybindSettingElement(Lang.KEY, null, supplier);
        this.keybindElement.visibleSupplier(() -> {
            return true;
        });
        this.bindModeToggle = new DualToggleElement(Lang.BIND_MODE, Lang.HOLD, Lang.TOGGLE);
        this.contentColumn.addFrameElement(this.bindModeToggle);
        this.contentColumn.addFrameElement(this.keybindElement);
        this.headerHeight = height();
        addChild(this.contentColumn);
    }

    public void bindType(Supplier<BindMode> supplier, Consumer<BindMode> consumer) {
        this.bindModeSupplier = supplier;
        if (supplier != null) {
            this.bindModeToggle.setState(supplier.get() == BindMode.HOLD);
        }
        this.bindModeToggle.setOnToggleCallback(bool -> {
            if (consumer != null) {
                consumer.accept(bool.booleanValue() ? BindMode.HOLD : BindMode.TOGGLE);
            }
        });
    }

    public void onChange(Consumer<List<Integer>> consumer) {
        this.keybindElement.onChange(consumer);
    }

    @Override
    public void render(DrawCtx class699Var) {
        if (this.openAnimation.isZero()) {
            return;
        }
        float f = totalHeight();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.openAnimation.smoothAnimation());
        class699Var.bloom(x(), y(), width(), f, 20.0f, Expensive.INSTANCE.windowController().bloom().getBloomFramebuffer().getColorAttachment());
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), f, 12.0f, 2.5f, class115VarColorStack.computeColor(ThemePalette.white.argb(), 0.03f), class115VarColorStack.computeColor(class764VarPalette.frameBackground().argb()));
        class699Var.texture(this.keyboardIcon, x() + 14.0f, (y() + (this.headerHeight / 2.0f)) - (this.keyboardIcon.height() / 2.0f), this.keyboardIcon.width(), this.keyboardIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
        float fX = x() + 14.0f + this.keyboardIcon.width() + 8.0f;
        float fY = (y() + (this.headerHeight / 2.0f)) - (this.font.getHeight(15.0f) / 2.0f);
        String str = this.labelText;
        float fTextWidthPhysical = class699Var.textWidthPhysical(this.font, str, 15);
        this.labelBounds.withPosition(fX, fY).withSize(fTextWidthPhysical, this.font.getHeight(15.0f));
        float fX2 = this.keybindBadge.x() - 30.0f;
        boolean z = fX + fTextWidthPhysical > fX2;
        String str2 = str;
        float fSmoothAnimation = z ? 1.0f - this.labelHoverAnimation.smoothAnimation() : 1.0f;
        if (fSmoothAnimation > 0.1f && z) {
            int iMax = Math.max(0, str.length() - ((int) Math.ceil(((fX + fTextWidthPhysical) - fX2) / (fTextWidthPhysical / str.length()))));
            String strSubstring = str.substring(0, iMax);
            if (iMax < str.length()) {
                strSubstring = strSubstring + "...";
            }
            str2 = strSubstring;
        }
        class699Var.text(this.font, str2, 15, fX, fY, class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
        class115VarColorStack.push();
        class115VarColorStack.alpha(fSmoothAnimation);
        this.keybindBadge.render(class699Var);
        class115VarColorStack.pop();
        this.contentColumn.render(class699Var);
        this.contentColumn.renderOverlays(class699Var);
        class115VarColorStack.pop();
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.keybindBadge.layout(class698Var);
        this.keybindBadge.setPosition(((x() + width()) - 14.0f) - this.keybindBadge.width(), (y() + (this.headerHeight / 2.0f)) - (this.keybindBadge.height() / 2.0f));
        this.contentColumn.setPosition(x() + 14.0f, this.labelBounds.y() + this.labelBounds.height() + 14.0f);
        this.keyboardIcon.setDimensions(14, 14);
        this.propertyIcon.setDimensions(10, 10);
        setSize(width(), this.headerHeight);
        super.layout(class698Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        if (this.openAnimation.isZero()) {
            return false;
        }
        boolean zInArea = class688Var.inArea(this.labelBounds.x(), this.labelBounds.y(), this.labelBounds.width(), this.labelBounds.height());
        if (this.labelBounds.x() + this.labelBounds.width() < (x() + width()) - 14.0f) {
            this.labelHoverAnimation.state(zInArea && !z);
        }
        if (super.handleInput(class688Var, z)) {
            return true;
        }
        if ((class688Var.inputEvent() instanceof CursorMoveInput) && !class688Var.inArea(x(), y(), width(), height() + contentHeight())) {
            return true;
        }
        if (class688Var.inputEvent() instanceof ScrollInput) {
            if (class688Var.inArea(x(), y(), width(), height() + contentHeight())) {
                return true;
            }
            closeWindow();
            return false;
        }
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof KeyInput) {
            KeyInput class696Var = (KeyInput) class691VarInputEvent;
            if (class696Var.keyAction().press() && class696Var.keyCode() == 256) {
                closeWindow();
                return true;
            }
        }
        InputEvent class691VarInputEvent2 = class688Var.inputEvent();
        if (!(class691VarInputEvent2 instanceof MouseButtonInput) || !((MouseButtonInput) class691VarInputEvent2).action().press()) {
            return false;
        }
        if (class688Var.inArea(x(), y(), width(), height() + contentHeight())) {
            return true;
        }
        closeWindow();
        return true;
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        class676Var.record(class699Var -> {
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            ThemePalette class764VarPalette = class699Var.theme().palette();
            class115VarColorStack.push();
            class115VarColorStack.alpha(this.openAnimation.smoothAnimation());
            class699Var.fillRoundedRect(x(), y(), width(), totalHeight(), 12.0f, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
            class115VarColorStack.pop();
        });
        super.collectBloomElements(class676Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.bindModeSupplier != null) {
            boolean z = this.bindModeSupplier.get() == BindMode.HOLD;
            if (this.bindModeToggle.isLeftActive() != z) {
                this.bindModeToggle.setState(z);
            }
        }
        this.openAnimation.animate(class141Var);
        this.labelHoverAnimation.animate(class141Var);
        super.animation(class141Var);
    }

    @Override
    public boolean isOpen() {
        return this.openAnimation.state() || !this.openAnimation.isZero();
    }

    public void openWindow() {
        if (isOpen()) {
            return;
        }
        this.openAnimation.state(true);
    }

    public void closeWindow() {
        if (isOpen()) {
            this.openAnimation.state(false);
            super.handleClose();
        }
    }

    public float totalHeight() {
        return this.headerHeight + Math.min(contentHeight(), 250.0f);
    }

    public float contentHeight() {
        if (this.contentColumn.height() <= 0.0f) {
            return 0.0f;
        }
        return 14.0f + this.contentColumn.height();
    }
}
