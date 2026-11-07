package aethereal.ui.setting;
import aethereal.resource.ClasspathResource;
import aethereal.ui.ClickableBehavior;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.model.DisplayNamed;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.event.EventCallback;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.render.HighlightAnimation;
import aethereal.model.InputEventContext;
import aethereal.event.LanguageChangeEvent;
import aethereal.model.LayoutScaleContext;
import aethereal.ui.ModuleFrame;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.model.ScrollInput;
import aethereal.type.SoundModeOption;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.lang.Enum;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class SoundModeSettingElement<T extends Enum<T>> extends ModuleFrame {
    public static final int titleFontSize = 14;
    public static final int descFontSize = 13;
    public static final float M = 3.0f;
    public static final float N = 12.0f;
    public static final float O = 25.0f;
    public final Translation name;
    public final Translation description;
    public boolean expanded;
    public Supplier<T> valueSupplier;
    public Consumer<T> changeConsumer;
    public Supplier<Boolean> visibleCondition;
    public static final float P = 23.0f;
    public static final float Q = 6.0f;
    public static final float R = 2.0f;
    public static final float S = 200.0f;
    public final MsdfFont titleFont = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont valueFont = Fonts.INTER_BOLD.get();
    public final MsdfFont descFont = Fonts.INTER_MEDIUM.get();
    public final ToggleAnimator dropdownAnimator = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
    public final HighlightAnimation highlightAnimation = new HighlightAnimation(300, Easings.EASE_IN_OUT_CUBIC);
    public final GlTexture enumIcon = new GlTexture(new ClasspathResource("/icons/menu/new/enum.png"));
    public final GlTexture arrowIcon = new GlTexture(new ClasspathResource("/icons/menu/new/arrow.png"));
    public final GlTexture checkmarkIcon = new GlTexture(new ClasspathResource("/icons/menu/new/checkmark.png"));
    public final GlTexture audioIcon = new GlTexture(new ClasspathResource("/icons/menu/new/audio.png"));
    public final ClickableBehavior clickable = new ClickableBehavior();
    public final List<SoundModeOption<T>> options = new ArrayList();
    public final WidgetBounds dropdownBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    public String wrappedDescription = null;
    public boolean descriptionDirty = true;
    public float lastLayoutWidth = -1.0f;
    public String lastDescription = null;

    public float descriptionHeight = 0.0f;

    public final EventCallback<LanguageChangeEvent> languageChangeCallback = class226Var -> {
        this.descriptionDirty = true;
        this.lastLayoutWidth = -1.0f;
        this.lastDescription = null;
    };

    public SoundModeSettingElement(Translation class254Var, Translation class254Var2, T[] tArr, Consumer<T> consumer) {
        this.name = class254Var;
        this.description = class254Var2;
        for (T t : tArr) {
            this.options.add(new SoundModeOption<>(t));
        }
        for (SoundModeOption<T> class834Var : this.options) {
            class834Var.clickableBehavior().clickCallback(() -> {
                if (this.valueSupplier != null && class834Var.option != this.valueSupplier.get()) {
                    invert();
                }
                if (this.changeConsumer != null) {
                    this.changeConsumer.accept((T) ((Enum) class834Var.option));
                }
            });
            class834Var.soundClickableBehavior.clickCallback(() -> {
                if (consumer != null) {
                    consumer.accept(class834Var.option);
                }
            });
        }
        this.clickable.clickCallback(this::invert);
        Expensive.INSTANCE.eventDispatcher().register(LanguageChangeEvent.class, this.languageChangeCallback);
    }

    @Override
    public void highlight() {
        this.highlightAnimation.trigger();
    }

    @Override
    public void clearHighlight() {
        this.highlightAnimation.clearHighlight();
    }

    @Override
    public void draw(DrawCtx class699Var, float f, float f2) {
        updateDescription(f);
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fY = y();
        float fValue = this.highlightAnimation.value();
        class699Var.text(this.titleFont, this.name.effective(), titleFontSize, x(), fY, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()), fValue));
        float f3 = 0.0f;
        if (this.wrappedDescription != null) {
            fY += this.titleFont.getHeight(14.0f) + M;
            class699Var.text(this.descFont, this.wrappedDescription, descFontSize, x(), fY, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb()), fValue));
            f3 = this.descriptionHeight;
        }
        float height = fY + (this.description != null ? f3 : this.titleFont.getHeight(14.0f)) + N;
        class699Var.fillOutlinedRoundedRect(x(), height, f, O, Q, 2.5f, class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(300).argb()), this.clickable.hoverAnimation()), class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()), this.clickable.hoverAnimation()));
        float fX = x() + 10.0f;
        class699Var.texture(this.enumIcon, fX, (height + 12.5f) - Math.round(this.enumIcon.height() / R), this.enumIcon.width(), this.enumIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()));
        float fX2 = ((x() + f) - 10.0f) - this.arrowIcon.width();
        float fRound = (height + 12.5f) - Math.round(this.arrowIcon.height() / R);
        float fWidth = fX2 + (this.arrowIcon.width() / R);
        float fHeight = fRound + (this.arrowIcon.height() / R);
        float fSmoothAnimation = (float) (3.141592653589793d * ((double) this.dropdownAnimator.smoothAnimation()));
        float physical = class699Var.layoutContext().toPhysical(fWidth);
        float physical2 = class699Var.layoutContext().toPhysical(fHeight);
        class699Var.matrixStack().push();
        class699Var.matrixStack().translate(physical, physical2, 0.0f);
        class699Var.matrixStack().multiply(new Quaternionf().rotateZ(fSmoothAnimation));
        class699Var.matrixStack().translate(-physical, -physical2, 0.0f);
        class699Var.texture(this.arrowIcon, fX2, fRound, this.arrowIcon.width(), this.arrowIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb()));
        class699Var.matrixStack().pop();
        if (this.valueSupplier == null) {
            return;
        }
        T t = this.valueSupplier.get();
        class699Var.texture(this.audioIcon, (x() + f) - 15.0f, y(), 15.0f, 15.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
        if (t instanceof DisplayNamed) {
            DisplayNamed class668Var = (DisplayNamed) t;
            float fWidth2 = fX + this.enumIcon.width() + Q;
            int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(300).argb());
            String strEffective = class668Var.getDisplayName().effective();
            float f4 = (fX2 - 10.0f) - fWidth2;
            if (class699Var.textWidthPhysical(this.valueFont, strEffective, 12) > f4) {
                int i = iComputeColor & 16777215;
                String str = "";
                for (int length = strEffective.length(); length > 0; length--) {
                    String strSubstring = strEffective.substring(0, length);
                    if (class699Var.textWidthPhysical(this.valueFont, strSubstring, 12) <= f4) {
                        str = strSubstring;
                        break;
                    }
                }
                class699Var.textWithHorizontalGradient(this.valueFont, str, 12, fWidth2, (height + 12.5f) - Math.round(this.valueFont.getHeight(N) / R), iComputeColor, i);
            } else {
                class699Var.text(this.valueFont, strEffective, 12, fWidth2, (height + 12.5f) - Math.round(this.valueFont.getHeight(N) / R), iComputeColor);
            }
        }
        float f5 = height + O + M;
        float fMethod006 = computeDropdownHeight();
        if (this.options.isEmpty() || (!this.expanded && this.dropdownAnimator.isZero())) {
            this.dropdownBounds.withSize(0.0f, 0.0f);
        } else {
            this.dropdownBounds.withPosition(x(), f5).withSize(f, fMethod006);
        }
        this.clickable.setDimensions(x(), y(), f, height());
    }

    @Override
    public void drawOverlay(DrawCtx class699Var) {
        if (this.options.isEmpty()) {
            return;
        }
        if (!this.dropdownAnimator.isZero() || this.expanded) {
            ThemePalette class764VarPalette = class699Var.theme().palette();
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            class115VarColorStack.push();
            class115VarColorStack.alpha(this.dropdownAnimator.smoothAnimation());
            class699Var.matrixStack().push();
            class699Var.matrixStack().translate(0.0f, 0.0f, S);
            float fHeight = this.dropdownBounds.height();
            float fY = this.dropdownBounds.y();
            float fWidth = this.dropdownBounds.width();
            class699Var.fillOutlinedRoundedRect(this.dropdownBounds.x(), fY, fWidth, fHeight, 7.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()));
            T t = this.valueSupplier == null ? null : this.valueSupplier.get();
            for (int i = 0; i < this.options.size(); i++) {
                SoundModeOption<T> class834Var = this.options.get(i);
                float f = fY + Q + (i * O);
                if (f + P > fY + fHeight) {
                    class834Var.clickableBehavior.setDimensions(0.0f, 0.0f, 0.0f, 0.0f);
                    break;
                }
                class834Var.clickableBehavior.setDimensions(this.dropdownBounds.x(), f, fWidth, P);
                boolean z = t != null && class834Var.option == t;
                int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb(), 0), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb(), 100), class834Var.clickableBehavior().hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb()), class834Var.currentOptionAnimation());
                float fX = this.dropdownBounds.x() + 5.0f;
                float f2 = fWidth - 10.0f;
                class699Var.fillRoundedRect(fX, f, f2, P, new Vector4f(Q, Q, Q, Q), iInterpolate);
                float f3 = ((fX + f2) - 5.0f) - 14.0f;
                int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(600).argb());
                if (z) {
                    float fWidth2 = ((fX + f2) - 5.0f) - this.checkmarkIcon.width();
                    class699Var.textureVerticalC(this.checkmarkIcon, fWidth2, f + 11.5f, this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb()));
                    f3 = (fWidth2 - Q) - 14.0f;
                    iComputeColor = class115VarColorStack.computeColor(class764VarPalette.text().tone(100).argb());
                }
                class834Var.soundClickableBehavior.setDimensions(f3 - 4.0f, ((f + 11.5f) - (14.0f / R)) - 4.0f, 14.0f + (4.0f * R), 14.0f + (4.0f * R));
                class699Var.textureVerticalC(this.audioIcon, f3, f + 11.5f, this.checkmarkIcon.width(), this.checkmarkIcon.height(), class115VarColorStack.interpolate(iComputeColor, class115VarColorStack.computeColor(class764VarPalette.text().tone(!z ? 300 : 50).argb()), class834Var.soundClickableBehavior.hoverAnimation()));
                T t2 = class834Var.option;
                if (t2 instanceof DisplayNamed) {
                    class699Var.text(this.valueFont, ((DisplayNamed) t2).getDisplayName().effective(), 12, this.dropdownBounds.x() + 5.0f + 5.0f, (f + 11.5f) - Math.round(this.valueFont.getHeight(N) / R), class115VarColorStack.computeColor(!z ? class764VarPalette.text().tone(500).argb() : class764VarPalette.text().tone(100).argb()));
                }
            }
            class699Var.matrixStack().pop();
            class115VarColorStack.pop();
        }
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        this.enumIcon.setDimensions(12, 12);
        this.arrowIcon.setDimensions(12, 12);
        this.checkmarkIcon.setDimensions(titleFontSize, titleFontSize);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.visibleCondition != null) {
            visible(this.visibleCondition.get().booleanValue());
        }
        this.dropdownAnimator.animate(class141Var);
        this.clickable.animate(class141Var);
        this.highlightAnimation.animate(class141Var);
        for (SoundModeOption<T> class834Var : this.options) {
            class834Var.clickableBehavior().animate(class141Var);
            class834Var.soundClickableBehavior.animate(class141Var);
            if (this.valueSupplier != null) {
                class834Var.currentOptionAnimation.state(class834Var.option == this.valueSupplier.get());
                class834Var.currentOptionAnimation.animate(class141Var);
            }
        }
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = false;
        if (this.expanded || !this.dropdownAnimator.isZero()) {
            for (int size = this.options.size() - 1; size >= 0; size--) {
                SoundModeOption<T> class834Var = this.options.get(size);
                boolean zHandleInput2 = zHandleInput | class834Var.soundClickableBehavior.handleInput(class688Var, z || zHandleInput);
                zHandleInput = zHandleInput2 | class834Var.clickableBehavior.handleInput(class688Var, z || zHandleInput2);
            }
            if (!z && !zHandleInput) {
                if ((class688Var.inputEvent() instanceof MouseButtonInput) && class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height())) {
                    return true;
                }
                if ((class688Var.inputEvent() instanceof CursorMoveInput) && class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height())) {
                    return true;
                }
            }
            if (class688Var.inputEvent() instanceof ScrollInput) {
                if (class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height())) {
                    return true;
                }
                closeDropdown();
                return true;
            }
            if (!z && (class688Var.inputEvent() instanceof MouseButtonInput)) {
                boolean zInArea = class688Var.inArea(this.clickable.ownerX(), this.clickable.ownerY(), this.clickable.ownerW(), this.clickable.ownerH());
                boolean zInArea2 = class688Var.inArea(this.dropdownBounds.x(), this.dropdownBounds.y(), this.dropdownBounds.width(), this.dropdownBounds.height());
                if (!zInArea && !zInArea2) {
                    closeDropdown();
                    return true;
                }
            }
        }
        if (this.clickable.handleInput(class688Var, z || zHandleInput)) {
            zHandleInput = true;
        }
        return zHandleInput;
    }

    @Override
    public void handleClose() {
        closeDropdown();
        Expensive.INSTANCE.eventDispatcher().unregister(LanguageChangeEvent.class, this.languageChangeCallback);
        super.handleClose();
    }

    @Override
    public void handleViewportVisibility(float f, float f2, float f3) {
        if (this.expanded || !this.dropdownAnimator.isZero()) {
            float fY = y() - f3;
            if (fY + height() < f || fY > f2) {
                closeDropdown();
            }
        }
    }

    @Override
    public float height() {
        if (this.description != null && this.descriptionDirty && this.parent != null) {
            updateDescription(this.parent.width());
        }
        float height = this.titleFont.getHeight(14.0f);
        if (this.description != null && this.wrappedDescription != null) {
            height += M + this.descriptionHeight;
        }
        return height + 37.0f;
    }

    public float computeDropdownHeight() {
        if (this.dropdownAnimator.isZero() && !this.expanded) {
            return 0.0f;
        }
        int size = this.options.size();
        return N + (size * P) + (Math.max(0, size - 1) * R);
    }

    public void closeDropdown() {
        if (this.expanded || !this.dropdownAnimator.isZero()) {
            this.expanded = false;
            this.dropdownAnimator.state(false);
        }
    }

    public void invert() {
        this.expanded = !this.expanded;
        this.dropdownAnimator.state(this.expanded);
    }

    public void updateDescription(float f) {
        if (this.description == null || this.description.effective() == null || this.description.effective().equals("null")) {
            this.wrappedDescription = null;
            this.descriptionHeight = 0.0f;
            this.descriptionDirty = false;
            this.lastLayoutWidth = f;
            this.lastDescription = null;
            return;
        }
        String strEffective = this.description.effective();
        if (this.descriptionDirty || f != this.lastLayoutWidth || strEffective == null || !strEffective.equals(this.lastDescription)) {
            if (strEffective == null) {
                strEffective = "";
            }
            this.wrappedDescription = StringUtil.formatTextToFitWidth(strEffective, f, this.descFont, descFontSize);
            this.descriptionHeight = this.descFont.getHeightWithLineBreaks(this.wrappedDescription, descFontSize);
            this.lastLayoutWidth = f;
            this.lastDescription = strEffective;
            this.descriptionDirty = false;
        }
    }

    public Translation name() {
        return this.name;
    }

    public SoundModeSettingElement<T> currentValueSupplier(Supplier<T> supplier) {
        this.valueSupplier = supplier;
        return this;
    }

    public SoundModeSettingElement<T> onChangeCallback(Consumer<T> consumer) {
        this.changeConsumer = consumer;
        return this;
    }

    public SoundModeSettingElement<T> visibleSupplier(Supplier<Boolean> supplier) {
        this.visibleCondition = supplier;
        return this;
    }
}
