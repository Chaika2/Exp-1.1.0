package aethereal.ui;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.net.InputInterceptor;
import aethereal.model.KeyInput;
import aethereal.model.LayoutScaleContext;
import aethereal.util.OverlayCommandQueue;
import aethereal.util.OverlayManager;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.net.TooltipService;
import aethereal.util.WeightedEngine;
import aethereal.util.WindowController;

public class MenuWindow extends AbstractWindow {
    public static float MENU_WIDTH = 920.0f;
    public static float MENU_HEIGHT = 550.0f;
    public static float EXPANDED_HEADER_HEIGHT = 256.0f;
    public static float COLLAPSED_HEADER_HEIGHT = 60.0f;
    public static final float[] scaleOptions = {0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f, 3.0f};
    public final GlTexture backgroundTexture;
    public boolean needsCentering;
    public final ToggleAnimator openAnimation;
    public final MenuHeaderContainer headerContainer;

    public final MenuContentArea contentArea;

    public final ChatPanel chat;

    public final DraggableContainer draggableBehavior;

    public final SearchOverlay searchContainer;

    public final ActionConfirmDialog actionConfirmationDialogContainer;

    public final PlainContainer tooltipLayer;

    public final TooltipService tooltipService;

    public final OverlayManager priorityOverlayHandler;
    public boolean menuOpen;

    public MenuWindow() {
        super(MENU_WIDTH, MENU_HEIGHT);
        this.backgroundTexture = new GlTexture(new ClasspathResource("/textures/background.png"));
        this.needsCentering = true;
        this.openAnimation = new ToggleAnimator(250, Easings.EASE_IN_OUT_CUBIC);
        this.headerContainer = new MenuHeaderContainer();
        this.draggableBehavior = new DraggableContainer();
        this.searchContainer = new SearchOverlay();
        this.actionConfirmationDialogContainer = new ActionConfirmDialog();
        this.tooltipLayer = new PlainContainer();
        this.tooltipService = new TooltipService(this.tooltipLayer);
        this.priorityOverlayHandler = new OverlayManager();
        this.menuOpen = false;
        this.contentArea = new MenuContentArea();
        this.visible = false;
        this.chat = new ChatPanel();
        addChild(this.draggableBehavior);
        addChild(this.chat);
        addChild(this.contentArea);
        addChild(this.tooltipLayer);
        addChild(this.headerContainer);
        addChild(this.searchContainer);
        addChild(this.actionConfirmationDialogContainer);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        if (this.needsCentering) {
            center(class698Var);
            this.needsCentering = false;
        }
        if (this.openAnimation.isZero()) {
            this.chat.visible(false);
            this.headerContainer.visible(false);
            this.contentArea.visible(false);
            this.draggableBehavior.visible(false);
            return;
        }
        this.chat.setPosition(x() + MENU_WIDTH, y());
        this.chat.visible(true);
        this.headerContainer.setPosition(x(), y());
        this.headerContainer.visible(true);
        this.contentArea.setPosition(x(), y() + COLLAPSED_HEADER_HEIGHT);
        this.contentArea.visible(true);
        this.searchContainer.setPosition(x(), y());
        this.searchContainer.setSize(MENU_WIDTH, MENU_HEIGHT);
        this.actionConfirmationDialogContainer.setPosition(x(), y());
        super.layout(class698Var);
        setSize(MENU_WIDTH + this.chat.width(), MENU_HEIGHT);
        this.draggableBehavior.setPosition(x(), y());
        this.draggableBehavior.setSize(width(), height());
        this.draggableBehavior.visible(true);
        super.onMenuDrag(this.draggableBehavior.isDragging());
    }

    @Override
    public void render(DrawCtx class699Var) {
        InputInterceptor class631VarWindow = class699Var.window();
        Expensive.INSTANCE.windowController().backgroundAlpha(this.openAnimation.isZero() ? 0 : Math.round(this.openAnimation.smoothAnimation() * 150.0f));
        if (!this.openAnimation.isZero()) {
            if (this.visible) {
                class631VarWindow.interceptCursorfScreenNotPresent(true);
                class631VarWindow.interceptKeyboardIfScreenPresent(true);
            }
            float fMethod004 = slideOffset();
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            class115VarColorStack.push();
            class699Var.matrixStack().push();
            if (!this.openAnimation.isOne()) {
                class699Var.matrixStack().translate(0.0f, class699Var.layoutContext().toPhysical(fMethod004), 0.0f);
                class115VarColorStack.alpha(this.openAnimation.smoothAnimation());
            }
            renderBackground(class699Var);
            boolean zExpandedState = this.headerContainer.expandedState();
            renderChild(this.draggableBehavior, class699Var);
            renderChild(this.chat, class699Var);
            renderChild(this.contentArea, class699Var);
            if (zExpandedState) {
                this.priorityOverlayHandler.renderPopup(class699Var);
                renderChild(this.tooltipLayer, class699Var);
            }
            renderChild(this.headerContainer, class699Var);
            if (!zExpandedState) {
                this.priorityOverlayHandler.renderPopup(class699Var);
                renderChild(this.tooltipLayer, class699Var);
            }
            renderChild(this.searchContainer, class699Var);
            renderChild(this.actionConfirmationDialogContainer, class699Var);
            class115VarColorStack.pop();
            class699Var.matrixStack().pop();
            if (class631VarWindow.cursorVisible()) {
                return;
            }
            this.openAnimation.state(false);
            this.menuOpen = false;
            close();
        }
    }

    public void renderChild(Widget class682Var, DrawCtx class699Var) {
        if (class682Var instanceof WidgetParent) {
            ((WidgetParent) class682Var).render(class699Var);
        }
    }

    public void renderBackground(DrawCtx class699Var) {
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.colorStack();
        class699Var.fillOutlinedRoundedRect(x(), y(), width(), height(), 8.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(700).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb()));
        class699Var.texture(this.backgroundTexture, x(), y(), MENU_WIDTH, height(), class115VarColorStack.computeColor(16777215));
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimation.animate(class141Var);
        boolean zIsZero = this.openAnimation.isZero();
        this.visible = !zIsZero;
        if (!zIsZero) {
            super.animation(class141Var);
            return;
        }
        this.chat.visible(false);
        this.headerContainer.visible(false);
        this.contentArea.visible(false);
        this.draggableBehavior.visible(false);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean z2 = this.menuOpen;
        boolean zHandleInput = z;
        boolean zExpandedState = this.headerContainer.expandedState();
        if (z2) {
            boolean zHandleInput2 = zHandleInput | this.actionConfirmationDialogContainer.handleInput(class688Var, zHandleInput);
            boolean zHandleInput3 = zHandleInput2 | this.searchContainer.handleInput(class688Var, zHandleInput2);
            if (!zExpandedState) {
                zHandleInput3 |= this.priorityOverlayHandler.handlePopupInput(class688Var, zHandleInput3);
            }
            boolean zHandleInput4 = zHandleInput3 | this.headerContainer.handleInput(class688Var, zHandleInput3);
            if (zExpandedState) {
                zHandleInput4 |= this.priorityOverlayHandler.handlePopupInput(class688Var, zHandleInput4);
            }
            boolean zHandleInput5 = zHandleInput4 | this.chat.handleInput(class688Var, zHandleInput4);
            boolean zHandleInput6 = zHandleInput5 | this.contentArea.handleInput(class688Var, zHandleInput5);
            zHandleInput = zHandleInput6 | this.draggableBehavior.handleInput(class688Var, zHandleInput6);
        }
        if (!zHandleInput) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof KeyInput) {
                KeyInput class696Var = (KeyInput) class691VarInputEvent;
                WindowController class686VarWindowController = Expensive.INSTANCE.windowController();
                if (class696Var.keyAction().press()) {
                    int iKeyCode = class696Var.keyCode();
                    if (iKeyCode == 260 || iKeyCode == 344) {
                        this.menuOpen = !this.menuOpen;
                        this.openAnimation.state(this.menuOpen);
                        if (this.menuOpen) {
                            class686VarWindowController.showCursor(class686VarWindowController.window());
                            class686VarWindowController.recenterMouse();
                        } else {
                            class686VarWindowController.revertCursor(class686VarWindowController.window());
                            class686VarWindowController.recenterMouse();
                        }
                        toggle();
                        return true;
                    }
                    if (iKeyCode == 256 && visible()) {
                        this.menuOpen = false;
                        this.openAnimation.state(false);
                        class686VarWindowController.revertCursor(class686VarWindowController.window());
                        class686VarWindowController.recenterMouse();
                        close();
                        return true;
                    }
                }
            }
        }
        return zHandleInput;
    }

    @Override
    public void collectBlurElements(OverlayCommandQueue class677Var) {
        if (this.openAnimation.isZero()) {
            return;
        }
        OverlayCommandQueue class677Var2 = new OverlayCommandQueue();
        super.collectBlurElements(class677Var2);
        class677Var.record(class699Var -> {
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            class115VarColorStack.push();
            class699Var.matrixStack().push();
            if (!this.openAnimation.isOne()) {
                class699Var.matrixStack().translate(0.0f, class699Var.layoutContext().toPhysical(slideOffset()), 0.0f);
                class115VarColorStack.alpha(this.openAnimation.smoothAnimation());
            }
            renderBackground(class699Var);
            class677Var2.renderRecorded(class699Var);
            class699Var.matrixStack().pop();
            class115VarColorStack.pop();
        });
    }

    @Override
    public void close() {
        ColorPickerWindow class773Var = (ColorPickerWindow) Expensive.INSTANCE.windowController().getWindow(ColorPickerWindow.class);
        if (class773Var != null) {
            class773Var.closePicker();
        }
        this.draggableBehavior.setDragging(false);
        super.close();
    }

    public void collapseExpandedHeader() {
        if (this.headerContainer.expandedState()) {
            this.headerContainer.exchange();
        }
    }

    public void requestCentering() {
        this.needsCentering = true;
    }

    public float slideOffset() {
        return (1.0f - this.openAnimation.smoothAnimation()) * 35.0f;
    }

    public MenuContentArea contentArea() {
        return this.contentArea;
    }

    public ChatPanel chat() {
        return this.chat;
    }

    public DraggableContainer draggableBehavior() {
        return this.draggableBehavior;
    }

    public SearchOverlay searchContainer() {
        return this.searchContainer;
    }

    public ActionConfirmDialog actionConfirmationDialogContainer() {
        return this.actionConfirmationDialogContainer;
    }

    public TooltipService tooltipService() {
        return this.tooltipService;
    }

    public OverlayManager priorityOverlayHandler() {
        return this.priorityOverlayHandler;
    }
}
