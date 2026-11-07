package aethereal.ui;
import aethereal.render.ColorStack;
import aethereal.render.DestraAssets;
import aethereal.render.DestraFonts;
import aethereal.render.DrawCtx;
import aethereal.render.GlTexture;
import aethereal.resource.ClasspathResource;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.event.InputEvent;
import aethereal.model.CharInput;
import aethereal.model.CursorMoveInput;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.model.LayoutScaleContext;
import aethereal.model.MouseButtonInput;
import aethereal.model.PixelPoint;
import aethereal.module.Module;
import aethereal.render.MsdfFont;
import aethereal.render.ToggleAnimator;
import aethereal.net.InputInterceptor;
import aethereal.type.MouseButtonAction;
import aethereal.util.WindowController;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SidebarMenuWindow extends AbstractWindow {
    public static final float WINDOW_WIDTH = 406.25f;
    public static final float WINDOW_HEIGHT = 281.25f;
    public static final float SIDEBAR_WIDTH = 125.0f;
    public static final float TOP_BAR_HEIGHT = 28.0f;
    public static final float TAB_HEIGHT = 18.0f;
    public static final float TAB_GAP = 1.0f;
    public static final float ROW_GAP = 3.0f;
    public static final int TOGGLE_KEY = 345;
    public static final int KEY_ESCAPE = 256;
    public static final int KEY_BACKSPACE = 259;

    public static final int COLOR_BACKGROUND = 0xF00A0A0C;
    public static final int COLOR_TOPBAR = 0xFF101013;
    public static final int COLOR_SIDEBAR = 0xFF0B0B0E;
    public static final int COLOR_BORDER = 0xFF232329;
    public static final int COLOR_SEARCH = 0xFF15151A;
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_SOFT = 0xCCFFFFFF;
    public static final int TEXT_MID = 0x99FFFFFF;
    public static final int TEXT_DIM = 0x66FFFFFF;

    public final ToggleAnimator openAnimation = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
    public final ScrollArea scrollArea = new ScrollArea();
    public final List<ModuleTab> tabs = List.of(ModuleTab.COMBAT, ModuleTab.MOVEMENT, ModuleTab.PLAYER, ModuleTab.RENDER, ModuleTab.MISC);
    public final List<ClickableBehavior> tabClicks = new ArrayList();
    public final Map<ModuleTab, List<ModuleRow>> rowsByTab = new EnumMap<>(ModuleTab.class);
    public final Map<ModuleTab, GlTexture> tabIcons = new EnumMap<>(ModuleTab.class);

    public MsdfFont titleFont;
    public MsdfFont bodyFont;
    public GlTexture logoTexture;

    public ModuleTab selectedTab = ModuleTab.COMBAT;
    public List<ModuleRow> activeRows = new ArrayList();
    public boolean menuOpen;
    public boolean needsCentering = true;
    public boolean dragging;
    public boolean searchFocused;
    public String searchText = "";
    public float dragOffsetX;
    public float dragOffsetY;
    public float contentHeight;

    public SidebarMenuWindow() {
        super(WINDOW_WIDTH, WINDOW_HEIGHT);
        this.visible = false;
        addChild(this.scrollArea);
        for (ModuleTab class847Var : this.tabs) {
            ClickableBehavior class820Var = new ClickableBehavior();
            class820Var.clickCallback(() -> selectTab(class847Var));
            this.tabClicks.add(class820Var);
        }
        this.activeRows = rowsFor(this.selectedTab);
    }

    public MsdfFont titleFont() {
        if (this.titleFont == null) {
            this.titleFont = DestraFonts.SUISSE_BOLD.get();
        }
        return this.titleFont;
    }

    public MsdfFont bodyFont() {
        if (this.bodyFont == null) {
            this.bodyFont = DestraFonts.SUISSE_SB.get();
        }
        return this.bodyFont;
    }

    public GlTexture logoTexture() {
        if (this.logoTexture == null) {
            try {
                this.logoTexture = DestraAssets.menuLogo();
            } catch (Throwable ignored) {
                this.logoTexture = null;
            }
        }
        return this.logoTexture;
    }

    public GlTexture tabIcon(ModuleTab class847Var) {
        if (this.tabIcons.containsKey(class847Var)) {
            return this.tabIcons.get(class847Var);
        }
        GlTexture class073Var;
        try {
            String str = class847Var == ModuleTab.CONFIGS ? "configs" : class847Var.getIconName();
            class073Var = new GlTexture(new ClasspathResource("/icons/menu/new/tabs/" + str + ".png"));
        } catch (Throwable ignored) {
            class073Var = null;
        }
        this.tabIcons.put(class847Var, class073Var);
        return class073Var;
    }

    public List<ModuleRow> rowsFor(ModuleTab class847Var) {
        return this.rowsByTab.computeIfAbsent(class847Var, class847Var2 -> {
            ArrayList arrayList = new ArrayList();
            Expensive.INSTANCE.moduleRepository().getModules().stream().filter(class605Var -> class605Var.getModuleTab() == class847Var2).sorted(Comparator.comparing(Module::getVisibleName, String.CASE_INSENSITIVE_ORDER)).forEach(class605Var2 -> {
                arrayList.add(new ModuleRow(class605Var2));
            });
            return arrayList;
        });
    }

    public void selectTab(ModuleTab class847Var) {
        if (class847Var == this.selectedTab) {
            return;
        }
        this.selectedTab = class847Var;
        this.searchText = "";
        this.searchFocused = false;
        this.activeRows = rowsFor(class847Var);
        this.scrollArea.scrollTo(0.0f);
    }

    public void updateSearch() {
        if (this.searchText.isEmpty()) {
            this.activeRows = rowsFor(this.selectedTab);
            this.scrollArea.scrollTo(0.0f);
            return;
        }
        String strLower = this.searchText.toLowerCase(Locale.ROOT);
        ArrayList arrayList = new ArrayList();
        for (ModuleTab class847Var : this.tabs) {
            for (ModuleRow class815Var : rowsFor(class847Var)) {
                if (class815Var.module().getVisibleName().toLowerCase(Locale.ROOT).contains(strLower)) {
                    arrayList.add(class815Var);
                }
            }
        }
        this.activeRows = arrayList;
        this.scrollArea.scrollTo(0.0f);
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        if (this.needsCentering) {
            center(class698Var);
            this.needsCentering = false;
        }
        if (this.openAnimation.isZero()) {
            this.scrollArea.visible(false);
            return;
        }
        this.scrollArea.visible(true);
        float fTabX = x() + 3.0f;
        float fTabWidth = SIDEBAR_WIDTH - 8.0f;
        float fTabY = y() + TOP_BAR_HEIGHT + 5.0f;
        for (ClickableBehavior class820Var : this.tabClicks) {
            class820Var.setDimensions(fTabX, fTabY, fTabWidth, TAB_HEIGHT);
            fTabY += TAB_HEIGHT + TAB_GAP;
        }
        float fContentX = x() + SIDEBAR_WIDTH + 5.0f;
        float fContentY = y() + TOP_BAR_HEIGHT + 5.0f;
        float fContentWidth = (WINDOW_WIDTH - SIDEBAR_WIDTH) - 10.0f;
        float fRowY = fContentY;
        for (ModuleRow class815Var : this.activeRows) {
            class815Var.rowWidth = fContentWidth;
            class815Var.setPosition(fContentX, fRowY);
            class815Var.layout(class698Var);
            fRowY += class815Var.height() + 3.0f;
        }
        this.contentHeight = Math.max(0.0f, (fRowY - fContentY) - 3.0f);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        super.layout(class698Var);
    }

    @Override
    public void render(DrawCtx class699Var) {
        InputInterceptor class631VarWindow = class699Var.window();
        WindowController class686VarWindowController = Expensive.INSTANCE.windowController();
        class686VarWindowController.backgroundAlpha(this.openAnimation.isZero() ? 0 : Math.round(this.openAnimation.smoothAnimation() * 120.0f));
        if (this.openAnimation.isZero()) {
            return;
        }
        if (this.visible) {
            class631VarWindow.interceptCursorfScreenNotPresent(true);
            class631VarWindow.interceptKeyboardIfScreenPresent(true);
        }
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class115VarColorStack.push();
        class699Var.matrixStack().push();
        if (!this.openAnimation.isOne()) {
            class699Var.matrixStack().translate(0.0f, class699Var.layoutContext().toPhysical(slideOffset()), 0.0f);
            class115VarColorStack.alpha(this.openAnimation.smoothAnimation());
        }
        renderFrame(class699Var);
        class115VarColorStack.pop();
        class699Var.matrixStack().pop();
        if (!class631VarWindow.cursorVisible()) {
            this.openAnimation.state(false);
            this.menuOpen = false;
            close();
        }
    }

    public void renderFrame(DrawCtx class699Var) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        int iAccent = class699Var.theme().palette().accent().argb();
        class699Var.fillOutlinedRoundedRect(x(), y(), WINDOW_WIDTH, WINDOW_HEIGHT, 6.0f, 1.0f,
                class115VarColorStack.computeColor(0xFF232329), class115VarColorStack.computeColor(0xF00A0A0C));
        renderTopBar(class699Var, iAccent);
        renderSidebar(class699Var, iAccent);
        renderContent(class699Var);
    }

    public void renderTopBar(DrawCtx class699Var, int iAccent) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.fillRoundedRect(x() + 1.0f, y() + 1.0f, WINDOW_WIDTH - 2.0f, TOP_BAR_HEIGHT - 1.0f, 5.0f, class115VarColorStack.computeColor(0xFF101013));
        class699Var.fillRect(x() + 6.0f, y() + 1.0f, WINDOW_WIDTH - 12.0f, 1.5f, class115VarColorStack.computeColor(iAccent, 0.55f));
        class699Var.fillRect(x() + 1.0f, y() + TOP_BAR_HEIGHT, WINDOW_WIDTH - 2.0f, 1.0f, class115VarColorStack.computeColor(0xFF232329));
        float fLogoX = x() + 8.0f;
        GlTexture class073Var = logoTexture();
        if (class073Var != null) {
            float fLogoW = 12.0f * (600.0f / 143.0f);
            class699Var.texture(class073Var, fLogoX, y() + ((TOP_BAR_HEIGHT - 12.0f) / 2.0f), fLogoX + fLogoW - fLogoX, 12.0f, class115VarColorStack.computeColor(0xFFFFFFFF));
            fLogoX += 12.0f * (600.0f / 143.0f) + 7.0f;
        }
        class699Var.text(titleFont(), this.selectedTab.getName().effective(), 9, fLogoX, y() + ((TOP_BAR_HEIGHT - titleFont().getHeight(9.0f)) / 2.0f), class115VarColorStack.computeColor(0xFFFFFFFF));
        renderSearch(class699Var, iAccent);
    }

    public void renderSidebar(DrawCtx class699Var, int iAccent) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        class699Var.fillRect(x() + 1.0f, y() + TOP_BAR_HEIGHT + 1.0f, SIDEBAR_WIDTH - 1.0f, WINDOW_HEIGHT - TOP_BAR_HEIGHT - 2.0f, class115VarColorStack.computeColor(0xFF0B0B0E));
        class699Var.fillRect(x() + SIDEBAR_WIDTH, y() + TOP_BAR_HEIGHT, 1.0f, WINDOW_HEIGHT - TOP_BAR_HEIGHT, class115VarColorStack.computeColor(0xFF232329));
        float fTabX = x() + 3.0f;
        float fTabWidth = SIDEBAR_WIDTH - 8.0f;
        float fTabY = y() + TOP_BAR_HEIGHT + 5.0f;
        for (int i = 0; i < this.tabs.size(); i++) {
            ModuleTab class847Var = this.tabs.get(i);
            boolean bl = class847Var == this.selectedTab && this.searchText.isEmpty();
            float fHover = this.tabClicks.get(i).hoverAnimation().smoothAnimation();
            if (bl) {
                class699Var.fillRoundedRect(fTabX, fTabY, fTabWidth, TAB_HEIGHT, 3.0f, class115VarColorStack.computeColor(iAccent, 0.16f));
                class699Var.fillRoundedRect(fTabX, fTabY + 3.0f, 2.0f, TAB_HEIGHT - 6.0f, 1.0f, class115VarColorStack.computeColor(iAccent));
            } else if (fHover > 0.001f) {
                class699Var.fillRoundedRect(fTabX, fTabY, fTabWidth, TAB_HEIGHT, 3.0f, class115VarColorStack.computeColor(0x14FFFFFF, fHover));
            }
            GlTexture class073Var = tabIcon(class847Var);
            float fTextX = fTabX + 7.0f;
            if (class073Var != null) {
                class699Var.texture(class073Var, fTabX + 6.0f, fTabY + ((TAB_HEIGHT - 9.0f) / 2.0f), 9.0f, 9.0f, class115VarColorStack.computeColor(bl ? iAccent : 0x99FFFFFF));
                fTextX = fTabX + 6.0f + 9.0f + 5.0f;
            }
            int iText = bl ? iAccent : class115VarColorStack.computeColor(0xCCFFFFFF);
            class699Var.text(bodyFont(), class847Var.getName().effective(), 8, fTextX, fTabY + ((TAB_HEIGHT - bodyFont().getHeight(8.0f)) / 2.0f), iText);
            fTabY += TAB_HEIGHT + TAB_GAP;
        }
    }

    public void renderContent(DrawCtx class699Var) {
        if (this.activeRows.isEmpty()) {
            ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
            float fX = x() + SIDEBAR_WIDTH + 5.0f;
            float fW = (WINDOW_WIDTH - SIDEBAR_WIDTH) - 10.0f;
            String str = this.searchText.isEmpty() ? "No modules" : "No results";
            class699Var.text(bodyFont(), str, 8, fX + (fW - class699Var.textWidthPhysical(bodyFont(), str, 8)) / 2.0f, y() + TOP_BAR_HEIGHT + 20.0f, class115VarColorStack.computeColor(0x66FFFFFF));
            return;
        }
        float fContentX = x() + SIDEBAR_WIDTH + 5.0f;
        float fContentY = y() + TOP_BAR_HEIGHT + 5.0f;
        float fContentWidth = (WINDOW_WIDTH - SIDEBAR_WIDTH) - 10.0f;
        float fContentHeight = (WINDOW_HEIGHT - TOP_BAR_HEIGHT) - 10.0f;
        this.scrollArea.beginArea(class699Var, fContentX, fContentY, fContentWidth, fContentHeight);
        for (ModuleRow class815Var : this.activeRows) {
            class815Var.render(class699Var);
        }
        this.scrollArea.endArea(class699Var, this.contentHeight);
    }

    public void renderSearch(DrawCtx class699Var) {
        renderSearch(class699Var, class699Var.theme().palette().accent().argb());
    }

    public void renderSearch(DrawCtx class699Var, int iAccent) {
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        float fX = searchFieldX();
        float fY = searchFieldY();
        float fW = searchFieldWidth();
        float fH = searchFieldHeight();
        class699Var.fillRoundedRect(fX, fY, fW, fH, 3.0f, class115VarColorStack.computeColor(0xFF15151A));
        class699Var.fillOutlinedRoundedRect(fX, fY, fW, fH, 3.0f, 1.0f, class115VarColorStack.computeColor(this.searchFocused ? iAccent : 0xFF232329), class115VarColorStack.computeColor(0xFF15151A));
        String str = this.searchText.isEmpty() ? "Search..." : this.searchText;
        int iText = class115VarColorStack.computeColor(this.searchText.isEmpty() ? 0x66FFFFFF : 0xFFFFFFFF);
        class699Var.text(bodyFont(), str, 8, fX + 6.0f, fY + ((fH - bodyFont().getHeight(8.0f)) / 2.0f), iText);
    }

    public float searchFieldX() {
        return x() + WINDOW_WIDTH - 8.0f - 110.0f;
    }

    public float searchFieldY() {
        return y() + 5.0f;
    }

    public float searchFieldWidth() {
        return 110.0f;
    }

    public float searchFieldHeight() {
        return 18.0f;
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimation.animate(class141Var);
        boolean zIsZero = this.openAnimation.isZero();
        this.visible = !zIsZero;
        if (!zIsZero) {
            for (ClickableBehavior class820Var : this.tabClicks) {
                class820Var.animate(class141Var);
            }
            for (ModuleRow class815Var : this.activeRows) {
                class815Var.animation(class141Var);
            }
            super.animation(class141Var);
            return;
        }
        this.scrollArea.visible(false);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) {
        boolean zHandleInput = z;
        if (this.menuOpen) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof MouseButtonInput) {
                MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
                if (class693Var.button() == 0 && class693Var.action().press() && !z) {
                    if (class688Var.inArea(searchFieldX(), searchFieldY(), searchFieldWidth(), searchFieldHeight())) {
                        this.searchFocused = true;
                        return true;
                    }
                    if (class688Var.inArea(x(), y(), WINDOW_WIDTH, TOP_BAR_HEIGHT)) {
                        this.searchFocused = false;
                    }
                }
            }
            if (this.searchFocused) {
                if (class688Var.inputEvent() instanceof CharInput) {
                    int iCodePoint = ((CharInput) class688Var.inputEvent()).codePoint();
                    if (iCodePoint >= 32 && iCodePoint != 127) {
                        this.searchText += new String(Character.toChars(iCodePoint));
                        updateSearch();
                        return true;
                    }
                } else if (class688Var.inputEvent() instanceof KeyInput) {
                    KeyInput class696Var = (KeyInput) class688Var.inputEvent();
                    if (class696Var.keyAction().press() && class696Var.keyCode() == 259 && !this.searchText.isEmpty()) {
                        this.searchText = this.searchText.substring(0, this.searchText.length() - 1);
                        updateSearch();
                        return true;
                    }
                }
            } else {
                InputEventContext class688VarWithMouseOffset = class688Var.withMouseOffset(0.0f, this.scrollArea.scrollY());
                for (ModuleRow class815Var : this.activeRows) {
                    if (class815Var.visible()) {
                        zHandleInput |= class815Var.handleInput(class688VarWithMouseOffset, zHandleInput);
                    }
                }
            }
            for (ClickableBehavior class820Var : this.tabClicks) {
                zHandleInput |= class820Var.handleInput(class688Var, zHandleInput);
            }
            zHandleInput |= handleDrag(class688Var, zHandleInput);
            zHandleInput |= this.scrollArea.handleInput(class688Var, zHandleInput);
        }
        if (!zHandleInput) {
            InputEvent class691VarInputEvent = class688Var.inputEvent();
            if (class691VarInputEvent instanceof KeyInput) {
                KeyInput class696Var = (KeyInput) class691VarInputEvent;
                WindowController class686VarWindowController = Expensive.INSTANCE.windowController();
                if (class696Var.keyAction().press()) {
                    int iKeyCode = class696Var.keyCode();
                    if (iKeyCode == TOGGLE_KEY) {
                        toggleMenu(class686VarWindowController);
                        return true;
                    }
                    if (iKeyCode == 256 && visible()) {
                        if (this.searchFocused) {
                            this.searchFocused = false;
                            return true;
                        }
                        closeMenu(class686VarWindowController);
                        return true;
                    }
                }
            }
        }
        return zHandleInput;
    }

    public boolean handleDrag(InputEventContext class688Var, boolean z) {
        InputEvent class691VarInputEvent = class688Var.inputEvent();
        if (class691VarInputEvent instanceof MouseButtonInput) {
            MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent;
            MouseButtonAction class706VarAction = class693Var.action();
            if (class693Var.button() == 0 && class706VarAction.press() && !z && class688Var.inArea(x(), y(), WINDOW_WIDTH, TOP_BAR_HEIGHT) && !class688Var.inArea(searchFieldX(), searchFieldY(), searchFieldWidth(), searchFieldHeight())) {
                this.dragging = true;
                this.dragOffsetX = x() - class688Var.logicalMousePosition().x();
                this.dragOffsetY = y() - class688Var.logicalMousePosition().y();
                return true;
            }
            if (class693Var.button() == 0 && class706VarAction.release() && this.dragging) {
                this.dragging = false;
                return true;
            }
        } else if (class688Var.inputEvent() instanceof CursorMoveInput) {
            if (this.dragging) {
                PixelPoint class708VarLogicalMousePosition = class688Var.logicalMousePosition();
                setPosition(class708VarLogicalMousePosition.x() + this.dragOffsetX, class708VarLogicalMousePosition.y() + this.dragOffsetY);
                return true;
            }
        }
        return false;
    }

    public void toggleMenu(WindowController class686VarWindowController) {
        if (this.menuOpen) {
            closeMenu(class686VarWindowController);
        } else if (this.openAnimation.isZero()) {
            openMenu(class686VarWindowController);
        } else {
            closeMenu(class686VarWindowController);
        }
    }

    public void openMenu(WindowController class686VarWindowController) {
        this.menuOpen = true;
        this.openAnimation.state(true);
        class686VarWindowController.showCursor(class686VarWindowController.window());
        class686VarWindowController.recenterMouse();
        toggle();
    }

    public void closeMenu(WindowController class686VarWindowController) {
        this.menuOpen = false;
        this.searchFocused = false;
        this.openAnimation.state(false);
        class686VarWindowController.revertCursor(class686VarWindowController.window());
        class686VarWindowController.recenterMouse();
        toggle();
    }

    public float slideOffset() {
        return (1.0f - this.openAnimation.smoothAnimation()) * 40.0f;
    }

    public ScrollArea scrollingArea() {
        return this.scrollArea;
    }
}
