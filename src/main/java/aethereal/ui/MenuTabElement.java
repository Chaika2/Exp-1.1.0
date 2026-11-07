package aethereal.ui;
import aethereal.ui.setting.ButtonSettingElement;
import aethereal.ui.setting.CheckboxSettingElement;
import aethereal.resource.ClasspathResource;
import aethereal.ui.setting.ColorSettingElement;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.util.FrameDimmingController;
import aethereal.render.GlTexture;
import aethereal.ui.setting.GroupSettingElement;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.ui.setting.KeybindSettingElement;
import aethereal.model.LayoutScaleContext;
import aethereal.util.MathUtil;
import aethereal.ui.setting.ModeSettingElement;
import aethereal.type.ModuleCategory;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.MultiSelectSettingElement;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.RenderCommandQueue;
import aethereal.ui.setting.Setting;
import aethereal.ui.setting.SliderSettingElement;
import aethereal.ui.setting.SoundModeSettingElement;
import aethereal.ui.setting.TextFieldSettingElement;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.net.TooltipService;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.joml.Vector4f;

public final class MenuTabElement extends Widget {
    public final MsdfFont SEMI_BOLD;
    public final ModuleTab moduleTab;
    public GlTexture icon;
    public final String iconName;
    public final Translation name;
    public final ClickableBehavior clickableBehavior;
    public final ToggleAnimator currentTabAnimation;
    public final List<AbstractFrame> frames;

    public final ScrollArea scrollingAreaComponent;
    public float framesOriginX;
    public float framesOriginY;
    public boolean framesLayoutDirty;
    public final Map<AbstractFrame, Float> frameHeights;
    public final FrameDimmingController dimmingManager;
    public AbstractFrame pendingScrollTarget;
    public int pendingScrollDelay;
    public boolean forceImmediateFramePositioning;
    public Set<ModuleCategory> activeCategories;
    public boolean active;
    public boolean inactiveTooltipShown;

    public TabLayout renderStrategy;
    public volatile boolean pendingFramesUpdate;
    public List<AbstractFrame> pendingNewFrames;
    public static final float inactiveAlpha = 0.7f;
    public static final Translation comingSoonText = Translation.clearText("Скоро появится");

    public MenuTabElement(ModuleTab class847Var, Translation class254Var, String str, TabLayout class800Var) {
        this.SEMI_BOLD = Fonts.INTER_SEMIBOLD.get();
        this.clickableBehavior = new ClickableBehavior();
        this.currentTabAnimation = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
        this.frames = new ArrayList();
        this.scrollingAreaComponent = new ScrollArea();
        this.framesLayoutDirty = true;
        this.frameHeights = new HashMap();
        this.dimmingManager = new FrameDimmingController();
        this.pendingScrollDelay = 0;
        this.activeCategories = Set.of();
        this.active = true;
        this.inactiveTooltipShown = false;
        this.pendingFramesUpdate = false;
        this.pendingNewFrames = new ArrayList();
        this.moduleTab = class847Var;
        this.name = class254Var;
        this.iconName = str;
        this.renderStrategy = class800Var;
        class800Var.initialize(this);
        this.clickableBehavior.clickCallback(() -> {
            Expensive.INSTANCE.tabsController().open(this);
        });
    }

    public MenuTabElement(ModuleTab class847Var, Translation class254Var, String str) {
        this(class847Var, class254Var, str, new ModuleGridLayout());
    }

    public void setRenderStrategy(TabLayout class800Var) {
        this.renderStrategy = class800Var;
        class800Var.initialize(this);
        this.framesLayoutDirty = true;
    }

    public void setActive(boolean z) {
        this.active = z;
        if (z && this.inactiveTooltipShown) {
            TooltipService class736Var = tooltipService();
            if (class736Var != null) {
                class736Var.hide(this);
            }
            this.inactiveTooltipShown = false;
        }
    }

    public String title() {
        return this.name.effective();
    }

    public void drawCollapsed(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.active ? 1.0f : inactiveAlpha);
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()), this.clickableBehavior.hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.accent().argb()), this.currentTabAnimation);
        GlTexture class073VarMethod003 = getIcon();
        class699Var.texture(class073VarMethod003, x(), y(), class073VarMethod003.width(), class073VarMethod003.height(), iInterpolate);
        this.clickableBehavior.setDimensions((x() + (class073VarMethod003.width() / 2.0f)) - (30.0f / 2.0f), (y() + (class073VarMethod003.height() / 2.0f)) - (30.0f / 2.0f), 30.0f, 30.0f);
        updateTooltipAnchor(class699Var);
        class115VarColorStack.pop();
    }

    public void drawExpanded(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.colorStack();
        ThemePalette class764VarPalette = class699Var.theme().palette();
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.active ? 1.0f : inactiveAlpha);
        int iInterpolate = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 0), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb(), 150), this.clickableBehavior.hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(700).argb()), this.currentTabAnimation);
        int iInterpolate2 = class115VarColorStack.interpolate(class115VarColorStack.interpolate(class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()), class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()), this.clickableBehavior.hoverAnimation()), class115VarColorStack.computeColor(class764VarPalette.accent().argb()), this.currentTabAnimation);
        class699Var.fillRoundedRect(x(), y(), expandedWidth(), expandedHeight(), new Vector4f(6.0f, 6.0f, 6.0f, 6.0f), iInterpolate);
        GlTexture class073VarMethod003 = getIcon();
        class699Var.texture(class073VarMethod003, x() + 12.0f, (y() + (expandedHeight() / 2.0f)) - (class073VarMethod003.height() / 2), class073VarMethod003.width(), class073VarMethod003.height(), iInterpolate2);
        class699Var.text(this.SEMI_BOLD, this.name.effective(), 12, x() + 12.0f + class073VarMethod003.width() + 8.0f, (y() + (expandedHeight() / 2.0f)) - (this.SEMI_BOLD.getHeight(12.0f) / 2.0f), iInterpolate2);
        this.clickableBehavior.setDimensions(x(), y(), expandedWidth(), expandedHeight());
        updateTooltipAnchor(class699Var);
        class115VarColorStack.pop();
    }

    public void drawFrames(DrawCtx class699Var) {
        if (this.renderStrategy != null) {
            this.renderStrategy.render(class699Var);
        }
    }

    public void renderFrameOverlays(DrawCtx class699Var) {
        if (this.renderStrategy != null) {
            this.renderStrategy.renderOverlays(class699Var);
        }
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        class677Var.record(this::drawFrames);
        Iterator<AbstractFrame> it = this.frames.iterator();
        while (it.hasNext()) {
            it.next().collectBlurElements(class677Var);
        }
    }

    @Override
    public void collectBloomElements(RenderCommandQueue class676Var) {
        Iterator<AbstractFrame> it = this.frames.iterator();
        while (it.hasNext()) {
            it.next().collectBloomElements(class676Var);
        }
        super.collectBloomElements(class676Var);
    }

    @Override
    public void onMenuDrag(boolean z) {
        this.frames.forEach(class757Var -> {
            class757Var.onMenuDrag(z);
        });
        super.onMenuDrag(z);
    }

    static int indexOfMin(float[] fArr) {
        int i = 0;
        float f = fArr[0];
        for (int i2 = 1; i2 < fArr.length; i2++) {
            if (fArr[i2] < f) {
                f = fArr[i2];
                i = i2;
            }
        }
        return i;
    }

    public void layout(LayoutScaleContext class698Var, float f, float f2) {
        if (this.framesOriginX != f || this.framesOriginY != f2) {
            this.framesOriginX = f;
            this.framesOriginY = f2;
            this.framesLayoutDirty = true;
            this.forceImmediateFramePositioning = true;
        }
        layoutFrames(class698Var);
    }

    public void layoutFrames(LayoutScaleContext class698Var) {
        if (this.renderStrategy != null) {
            this.renderStrategy.layout(class698Var);
        } else {
            this.frames.forEach(class757Var -> {
                class757Var.layout(class698Var);
            });
        }
        for (AbstractFrame class757Var2 : this.frames) {
            float fHeight = class757Var2.height() + class757Var2.contentHeight();
            Float f = this.frameHeights.get(class757Var2);
            if (f == null || Math.abs(f.floatValue() - fHeight) > 0.01f) {
                this.frameHeights.put(class757Var2, Float.valueOf(fHeight));
                this.framesLayoutDirty = true;
            }
        }
        if (this.framesLayoutDirty) {
            positionFrames();
            this.framesLayoutDirty = false;
            this.forceImmediateFramePositioning = false;
        }
        getIcon().setDimensions(14, 14);
    }

    public void positionFrames() {
        if (this.renderStrategy != null) {
            this.renderStrategy.positionFrames();
        }
    }

    public boolean handleInputFrames(InputEventContext class688Var, boolean z) {
        return this.renderStrategy != null ? this.renderStrategy.handleInput(class688Var, z) : z;
    }

    public float contentHeight() {
        if (this.renderStrategy != null) {
            return this.renderStrategy.getContentHeight();
        }
        return 0.0f;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        layoutFrames(class698Var);
    }

    public void markFramesDirty() {
        this.framesLayoutDirty = true;
    }

    public void focusFrame(AbstractFrame class757Var) {
        if (class757Var == null || !this.frames.contains(class757Var)) {
            return;
        }
        this.framesLayoutDirty = true;
        this.forceImmediateFramePositioning = true;
        this.pendingScrollTarget = class757Var;
        this.pendingScrollDelay = 2;
        if (class757Var instanceof ModuleCard) {
            ((ModuleCard) class757Var).highlight();
        }
        this.dimmingManager.focusFrame(class757Var);
    }

    public void focusSetting(AbstractFrame class757Var, Setting class661Var) {
        if (class757Var == null || !this.frames.contains(class757Var)) {
            return;
        }
        this.framesLayoutDirty = true;
        this.forceImmediateFramePositioning = true;
        this.pendingScrollTarget = class757Var;
        this.pendingScrollDelay = 2;
        this.dimmingManager.focusFrame(class757Var);
        FrameElementColumn class816VarFrameElementsContainer = class757Var.frameElementsContainer();
        if (class816VarFrameElementsContainer != null) {
            for (Widget class682Var : class816VarFrameElementsContainer.children()) {
                if (class682Var instanceof ModuleFrame) {
                    ModuleFrame class758Var = (ModuleFrame) class682Var;
                    if (matchesSetting(class758Var, class661Var)) {
                        class757Var.focusElement(class758Var);
                        return;
                    }
                }
            }
        }
    }

    static int typeSwitch00732(Object o) {
        if (o == null) return -1;
        if (o instanceof CheckboxSettingElement) return 0;
        if (o instanceof SliderSettingElement) return 1;
        if (o instanceof ModeSettingElement) return 2;
        if (o instanceof SoundModeSettingElement) return 3;
        if (o instanceof KeybindSettingElement) return 4;
        if (o instanceof MultiSelectSettingElement) return 5;
        if (o instanceof ColorSettingElement) return 6;
        if (o instanceof GroupSettingElement) return 7;
        if (o instanceof TextFieldSettingElement) return 8;
        if (o instanceof ButtonSettingElement) return 9;
        return 10;
    }

    public boolean matchesSetting(ModuleFrame class758Var, Setting class661Var) {
        String strEffective;
        Objects.requireNonNull(class758Var);
        switch (typeSwitch00732(class758Var)) {
            case 0:
                strEffective = ((CheckboxSettingElement) class758Var).name().effective();
                break;
            case 1:
                strEffective = ((SliderSettingElement) class758Var).name().effective();
                break;
            case 2:
                strEffective = ((ModeSettingElement) class758Var).name().effective();
                break;
            case 3:
                strEffective = ((SoundModeSettingElement) class758Var).name().effective();
                break;
            case 4:
                strEffective = ((KeybindSettingElement) class758Var).name().effective();
                break;
            case 5:
                strEffective = ((MultiSelectSettingElement) class758Var).name().effective();
                break;
            case 6:
                strEffective = ((ColorSettingElement) class758Var).name().effective();
                break;
            case 7:
                strEffective = ((GroupSettingElement) class758Var).name().effective();
                break;
            case 8:
                strEffective = ((TextFieldSettingElement) class758Var).name().effective();
                break;
            case 9:
                strEffective = ((ButtonSettingElement) class758Var).name().effective();
                break;
            default:
                strEffective = null;
                break;
        }
        String str = strEffective;
        return str != null && str.equals(class661Var.getName().effective());
    }

    public void newFrame(AbstractFrame class757Var) {
        this.frames.add(class757Var);
        updateFrameVisibility(class757Var);
        this.framesLayoutDirty = true;
    }

    public void applyCategoryFilter(Set<ModuleCategory> set) {
        this.activeCategories = set == null ? Set.of() : Set.copyOf(set);
        this.frames.forEach(this::updateFrameVisibility);
        this.framesLayoutDirty = true;
    }

    public void updateFrameVisibility(AbstractFrame class757Var) {
        if (!(class757Var instanceof ModuleCard)) {
            class757Var.visible(true);
        } else {
            ModuleCategory category = ((ModuleCard) class757Var).module().getCategory();
            class757Var.visible(this.activeCategories.isEmpty() || (category != null && this.activeCategories.contains(category)));
        }
    }

    public void scrollToPendingTarget() {
        if (this.pendingScrollTarget == null) {
            return;
        }
        float f = MenuWindow.MENU_HEIGHT - MenuWindow.COLLAPSED_HEADER_HEIGHT;
        float fY = this.pendingScrollTarget.y();
        if (fY < 0.1f) {
            this.pendingScrollDelay = 1;
            return;
        }
        this.scrollingAreaComponent.scrollTo(MathUtil.clamp(Math.max(0.0f, (fY - this.framesOriginY) - 10.0f), 0.0f, Math.max(0.0f, contentHeight() - f)));
        this.pendingScrollTarget = null;
        this.pendingScrollDelay = 0;
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        AbstractFrame class757VarFocusedFrame;
        boolean zHandleInput = super.handleInput(class688Var, z);
        boolean zHandleInput2 = zHandleInput | this.clickableBehavior.handleInput(class688Var, zHandleInput);
        updateInactiveTooltip(class688Var);
        if (this.dimmingManager.isDimming() && (class757VarFocusedFrame = this.dimmingManager.focusedFrame()) != null) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if ((class691VarInputEvent instanceof MouseButtonInput) && ((MouseButtonInput) class691VarInputEvent).action().press() && !class688Var.inArea(class757VarFocusedFrame.x(), class757VarFocusedFrame.y(), class757VarFocusedFrame.width(), class757VarFocusedFrame.height() + class757VarFocusedFrame.contentHeight())) {
                clearHighlight();
            }
        }
        return zHandleInput2;
    }

    public void clearHighlight() {
        this.dimmingManager.clearFocus();
        for (AbstractFrame class757Var : this.frames) {
            if (class757Var instanceof ModuleCard) {
                ((ModuleCard) class757Var).highlightAnimation().clearHighlight();
            }
            class757Var.clearElementFocus();
        }
    }

    @Override
    public void handleClose() {
        this.frames.forEach((v0) -> {
            v0.handleClose();
        });
        clearHighlight();
        super.handleClose();
    }

    public void updateInactiveTooltip(InputEventContext class688Var) {
        TooltipService class736Var = tooltipService();
        if (class736Var == null) {
            return;
        }
        if (this.active) {
            if (this.inactiveTooltipShown) {
                this.inactiveTooltipShown = false;
                class736Var.hide(this);
                return;
            }
            return;
        }
        boolean zState = this.clickableBehavior.hoverAnimation().state();
        if (class688Var.inputEvent() instanceof CursorMoveInput) {
            if (zState && !this.inactiveTooltipShown) {
                this.inactiveTooltipShown = true;
                class736Var.show(this, tooltipBounds(), comingSoonText, null);
            } else {
                if (zState || !this.inactiveTooltipShown) {
                    return;
                }
                this.inactiveTooltipShown = false;
                class736Var.hide(this);
            }
        }
    }

    public void updateTooltipAnchor(DrawCtx class699Var) {
        TooltipService class736Var = tooltipService();
        if (!this.inactiveTooltipShown || this.active || class736Var == null || !class736Var.isOwnedBy(this)) {
            return;
        }
        class736Var.updateAnchor(this, tooltipBounds(), class699Var);
    }

    public TooltipService tooltipService() {
        MenuWindow class776VarMenuWindow = Expensive.INSTANCE.menuWindow();
        if (class776VarMenuWindow == null) {
            return null;
        }
        return class776VarMenuWindow.tooltipService();
    }

    public WidgetBounds tooltipBounds() {
        return new WidgetBounds(this.clickableBehavior.ownerX(), this.clickableBehavior.ownerY() + 5.0f, this.clickableBehavior.ownerW(), this.clickableBehavior.ownerH());
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        if (this.pendingFramesUpdate) {
            this.frames.clear();
            this.frames.addAll(this.pendingNewFrames);
            this.pendingNewFrames.clear();
            this.pendingFramesUpdate = false;
            this.framesLayoutDirty = true;
        }
        this.clickableBehavior.animate(class141Var);
        this.currentTabAnimation.state(this == Expensive.INSTANCE.tabsController().current());
        this.currentTabAnimation.animate(class141Var);
        this.scrollingAreaComponent.animation(class141Var);
        float fVisibleTop = this.scrollingAreaComponent.visibleTop();
        float fVisibleBottom = this.scrollingAreaComponent.visibleBottom();
        for (AbstractFrame class757Var : this.frames) {
            if (class757Var.visible()) {
                class757Var.updateViewportVisibility(fVisibleTop, fVisibleBottom, this.framesOriginY);
            }
        }
        if (this.renderStrategy != null) {
            this.renderStrategy.animation(class141Var);
        }
        this.frames.forEach(class757Var2 -> {
            class757Var2.animation(class141Var);
        });
        this.dimmingManager.animate(class141Var);
        if (this.pendingScrollTarget != null && this.pendingScrollDelay > 0) {
            this.pendingScrollDelay--;
            if (this.pendingScrollDelay == 0) {
                scrollToPendingTarget();
            }
        }
        super.animation(class141Var);
    }

    public void updateFramesSafe(List<AbstractFrame> list) {
        this.pendingNewFrames.clear();
        this.pendingNewFrames.addAll(list);
        this.pendingFramesUpdate = true;
    }

    public float collapsedWidth() {
        return getIcon().width();
    }

    public float collapsedHeight() {
        return getIcon().height();
    }

    public float expandedHeight() {
        return 35.0f;
    }

    public float expandedWidth() {
        return 110.0f;
    }

    public GlTexture getIcon() {
        if (this.icon == null) {
            this.icon = new GlTexture(new ClasspathResource("/icons/menu/new/tabs/%s.png".formatted(this.iconName))).setDimensions(14, 14);
        }
        return this.icon;
    }

    @Override
    public float width() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float height() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void open() {
    }

    public MsdfFont SEMI_BOLD() {
        return this.SEMI_BOLD;
    }

    public ModuleTab moduleTab() {
        return this.moduleTab;
    }

    public GlTexture icon() {
        return this.icon;
    }

    public String iconName() {
        return this.iconName;
    }

    public Translation name() {
        return this.name;
    }

    public ClickableBehavior clickableBehavior() {
        return this.clickableBehavior;
    }

    public ToggleAnimator currentTabAnimation() {
        return this.currentTabAnimation;
    }

    public List<AbstractFrame> frames() {
        return this.frames;
    }

    public ScrollArea scrollingAreaComponent() {
        return this.scrollingAreaComponent;
    }

    public float framesOriginX() {
        return this.framesOriginX;
    }

    public float framesOriginY() {
        return this.framesOriginY;
    }

    public boolean framesLayoutDirty() {
        return this.framesLayoutDirty;
    }

    public Map<AbstractFrame, Float> frameHeights() {
        return this.frameHeights;
    }

    public FrameDimmingController dimmingManager() {
        return this.dimmingManager;
    }

    public AbstractFrame pendingScrollTarget() {
        return this.pendingScrollTarget;
    }

    public int pendingScrollDelay() {
        return this.pendingScrollDelay;
    }

    public boolean forceImmediateFramePositioning() {
        return this.forceImmediateFramePositioning;
    }

    public Set<ModuleCategory> activeCategories() {
        return this.activeCategories;
    }

    public boolean active() {
        return this.active;
    }

    public boolean inactiveTooltipShown() {
        return this.inactiveTooltipShown;
    }

    public boolean pendingFramesUpdate() {
        return this.pendingFramesUpdate;
    }

    public List<AbstractFrame> pendingNewFrames() {
        return this.pendingNewFrames;
    }

    public TabLayout renderStrategy() {
        return this.renderStrategy;
    }
}
