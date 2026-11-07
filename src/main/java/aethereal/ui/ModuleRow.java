package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DestraFonts;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.render.MsdfFont;
import aethereal.render.ToggleAnimator;
import aethereal.module.Module;
import aethereal.model.InputEventContext;
import aethereal.model.LayoutScaleContext;
import aethereal.util.ConfigAutoSaveScheduler;
import aethereal.util.KeyboardUtil;
import aethereal.util.WeightedEngine;

public class ModuleRow extends AbstractFrame {
    public static final float headerHeight = 18.0f;
    public static final float padding = 5.0f;
    public static final float cornerRadius = 3.0f;
    public static final float toggleWidth = 18.0f;
    public static final float toggleHeight = 11.0f;
    public static final int CARD = 0xFF141419;
    public static final int CARD_HOVER = 0xFF1C1C23;
    public static final int BORDER = 0xFF232329;

    public final Module module;
    public final ToggleSwitch toggleSwitch;
    public final ClickableBehavior headerClick = new ClickableBehavior();
    public final ToggleAnimator expandAnimation = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
    public MsdfFont font;
    public boolean expanded;
    public boolean settingsBuilt;
    public float rowWidth = 300.0f;

    public ModuleRow(Module class605Var) {
        super(new FrameElementColumn(280.0f, 6.0f));
        this.module = class605Var;
        this.toggleSwitch = new ToggleSwitch(toggleWidth, toggleHeight, class605Var::isState);
        this.toggleSwitch.runnable(() -> {
            class605Var.switchState();
            ConfigAutoSaveScheduler.scheduleAutoSave();
        });
        this.headerClick.clickCallback(this::toggleExpanded);
        this.frameElementsContainer.visible(false);
        addChild(this.toggleSwitch);
    }

    public MsdfFont font() {
        if (this.font == null) {
            this.font = DestraFonts.SUISSE_SB.get();
        }
        return this.font;
    }

    public void toggleExpanded() {
        if (!this.expanded && !this.settingsBuilt) {
            ModuleSettingsBuilder.populate(this.module, this.frameElementsContainer);
            this.settingsBuilt = true;
        }
        this.expanded = !this.expanded;
        this.expandAnimation.state(this.expanded);
        this.frameElementsContainer.visible(this.expanded);
    }

    @Override
    public void render(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iAccent = class699Var.theme().palette().accent().argb();
        float fWidth = width();
        float fHover = this.headerClick.hoverAnimation().smoothAnimation();
        boolean bl = this.module.isState();
        int iBg = class115VarColorStack.interpolate(class115VarColorStack.computeColor(bl ? CARD : 0xFF111115), class115VarColorStack.computeColor(CARD_HOVER), fHover);
        int iBorder = class115VarColorStack.computeColor(fHover > 0.001f ? iAccent : BORDER, 0.25f + 0.45f * fHover);
        if (this.settingsBuilt && this.expandAnimation.smoothAnimation() > 0.001f) {
            class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, height(), cornerRadius, 1.0f, iBorder, class115VarColorStack.computeColor(0xFF101014));
            class115VarColorStack.push();
            class115VarColorStack.alpha(this.expandAnimation.smoothAnimation());
            this.frameElementsContainer.render(class699Var);
            class115VarColorStack.pop();
        }
        class699Var.fillOutlinedRoundedRect(x(), y(), fWidth, headerHeight, cornerRadius, 1.0f, iBorder, iBg);
        if (bl) {
            class699Var.fillRoundedRect(x() + 3.0f, y() + 3.5f, 2.0f, headerHeight - 7.0f, 1.0f, class115VarColorStack.computeColor(iAccent));
        }
        String strName = this.module.getVisibleName();
        float fTextX = x() + padding + (bl ? 4.0f : 0.0f);
        int iName = class115VarColorStack.computeColor(bl ? 0xFFFFFFFF : 0x99FFFFFF);
        class699Var.text(font(), strName, 8, fTextX, y() + ((headerHeight - font().getHeight(8.0f)) / 2.0f), iName);
        if (this.module.hasKeyBind()) {
            String str = KeyboardUtil.keysToString(this.module.getKeyBind());
            float fW = class699Var.textWidthPhysical(font(), str, 7);
            class699Var.text(font(), str, 7, this.toggleSwitch.x() - 6.0f - fW, y() + ((headerHeight - font().getHeight(7.0f)) / 2.0f), class115VarColorStack.computeColor(0x66FFFFFF));
        }
        this.toggleSwitch.render(class699Var);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        float fToggleX = ((x() + width()) - padding) - toggleWidth;
        float fToggleY = y() + ((headerHeight - toggleHeight) / 2.0f);
        this.toggleSwitch.setPosition(fToggleX, fToggleY);
        this.toggleSwitch.setClickableArea(fToggleX, fToggleY, toggleWidth, toggleHeight);
        this.headerClick.setDimensions(x(), y(), width(), headerHeight);
        this.frameElementsContainer.setWidth(width() - (padding * 2.0f));
        this.frameElementsContainer.setPosition(x() + padding, y() + headerHeight + padding);
        super.layout(class698Var);
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.expandAnimation.animate(class141Var);
        this.headerClick.animate(class141Var);
        this.frameElementsContainer.visible(this.expanded || !this.expandAnimation.isZero());
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = super.handleInput(class688Var, z);
        if (!zHandleInput) {
            zHandleInput = this.headerClick.handleInput(class688Var, z);
        }
        return zHandleInput;
    }

    @Override
    public float contentHeight() {
        if (!this.settingsBuilt) {
            return 0.0f;
        }
        return this.frameElementsContainer.height() + (padding * 2.0f);
    }

    @Override
    public float width() {
        return this.rowWidth;
    }

    @Override
    public float height() {
        return headerHeight + (this.expandAnimation.smoothAnimation() * contentHeight());
    }

    public Module module() {
        return this.module;
    }
}
