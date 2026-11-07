package aethereal.ui;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ButtonSetting;
import aethereal.resource.ClasspathResource;
import aethereal.ui.setting.ColorSetting;
import aethereal.render.ColorStack;
import aethereal.model.CursorMoveInput;
import aethereal.render.DrawCtx;
import aethereal.math.Easings;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.model.FrameSearchTarget;
import aethereal.render.GlTexture;
import aethereal.event.InputEvent;
import aethereal.model.InputEventContext;
import aethereal.model.KeyInput;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.model.LayoutScaleContext;
import aethereal.type.MatchMode;
import aethereal.util.MenuSearchNavigator;
import aethereal.ui.setting.ModeSetting;
import aethereal.type.ModuleCategory;
import aethereal.model.MouseButtonInput;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.model.PixelPoint;
import aethereal.model.ScrollInput;
import aethereal.model.SearchMatch;
import aethereal.model.SearchNavTarget;
import aethereal.util.SearchNavigator;
import aethereal.ui.setting.Setting;
import aethereal.model.SettingSearchTarget;
import aethereal.util.TabsController;
import aethereal.ui.setting.TextFieldSetting;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.Translation;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public class SearchOverlay extends WidgetContainer {
    public static final float smallGap = 2.0f;
    public static final float rowHeight = 28.0f;
    public static final float horizontalPadding = 10.0f;
    public static final float fontSize = 13.0f;
    public static final float groupSpacing = 12.0f;
    public static final float maxHeight = 450.0f;
    public static final float cornerRadius = 8.0f;
    public static final float epsilon = 0.01f;
    public static final int iconSize = 13;
    boolean opened;

    final ScrollbarWidget scrollbar;
    public final GlTexture searchIcon = new GlTexture(new ClasspathResource("/icons/menu/new/search.png")).setDimensions(iconSize, iconSize);
    public final GlTexture frameIcon = new GlTexture(new ClasspathResource("/icons/menu/new/frame.png")).setDimensions(12, 12);
    public final GlTexture multiEnumIcon = new GlTexture(new ClasspathResource("/icons/menu/new/multienum.png")).setDimensions(12, 12);
    public final GlTexture sliderIcon = new GlTexture(new ClasspathResource("/icons/menu/new/slider.png")).setDimensions(12, 12);
    public final GlTexture enumIcon = new GlTexture(new ClasspathResource("/icons/menu/new/enum.png")).setDimensions(12, 12);
    public final GlTexture arrowIcon = new GlTexture(new ClasspathResource("/icons/menu/new/arrow_horizontal.png")).setDimensions(9, 8);
    public final GlTexture gamepadIcon = new GlTexture(new ClasspathResource("/icons/menu/new/gamepad.png")).setDimensions(12, 12);
    public final GlTexture textIcon = new GlTexture(new ClasspathResource("/icons/menu/new/text.png")).setDimensions(12, 12);
    public final GlTexture groupIcon = new GlTexture(new ClasspathResource("/icons/menu/new/group.png")).setDimensions(12, 12);
    public final GlTexture brushIcon = new GlTexture(new ClasspathResource("/icons/menu/new/brush.png")).setDimensions(12, 12);
    public final GlTexture clickIcon = new GlTexture(new ClasspathResource("/icons/menu/new/click.png")).setDimensions(12, 12);
    public boolean keyboardNavigation = false;
    public float lastMouseX = Float.NaN;
    public float lastMouseY = Float.NaN;
    public final MsdfFont font = Fonts.INTER_SEMIBOLD.get();
    public final MsdfFont boldFont = Fonts.INTER_EXTRA_BOLD.get();
    final TextInputField searchField = new TextInputField(this.font, iconSize);
    final ToggleAnimator openAnimator = new ToggleAnimator(220, Easings.EASE_IN_OUT_CUBIC);
    final WidgetBounds searchBoxBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    final WidgetBounds resultsBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
    final List<SearchMatch> matches = new ArrayList();
    final List<SearchResultRow> resultRows = new ArrayList();
    public final SearchNavigator navigator = new MenuSearchNavigator();
    final ScrollArea scrollArea = new ScrollArea();
    public int selectedIndex = -1;
    public final ToggleAnimator selectionAnimator = new ToggleAnimator(150, Easings.EASE_IN_OUT_CUBIC);
    public final List<ClickableBehavior> clickBehaviors = new ArrayList();

    public SearchOverlay() {
        ScrollArea class789Var = this.scrollArea;
        Objects.requireNonNull(class789Var);
        Supplier supplier = class789Var::scrollY;
        Supplier supplier2 = this::getContentHeight;
        WidgetBounds class678Var = this.resultsBounds;
        Objects.requireNonNull(class678Var);
        Supplier supplier3 = class678Var::height;
        ScrollArea class789Var2 = this.scrollArea;
        Objects.requireNonNull(class789Var2);
        this.scrollbar = new ScrollbarWidget(supplier, supplier2, supplier3, (v1) -> {
            class789Var.scrollTo(v1);
        }, cornerRadius, 4.0f, 30.0f);
        addChild(this.scrollArea);
        addChild(this.scrollbar);
        this.searchField.changeText(str -> {
            this.matches.clear();
            String strReplaceAll = str.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            boolean zIsBlank = strReplaceAll.isBlank();
            TabsController class733VarTabsController = Expensive.INSTANCE.tabsController();
            if (!zIsBlank) {
                for (MenuTabElement class732Var : class733VarTabsController.tabElements()) {
                    for (AbstractFrame class757Var : class732Var.frames()) {
                        if (class757Var instanceof ModuleCard) {
                            ModuleCard class817Var = (ModuleCard) class757Var;
                            String strReplaceAll2 = class817Var.name().toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
                            MatchMode class793Var = null;
                            if (strReplaceAll2.equals(strReplaceAll)) {
                                class793Var = MatchMode.EXACT;
                            } else if (strReplaceAll2.startsWith(strReplaceAll)) {
                                class793Var = MatchMode.STARTS_WITH;
                            } else if (strReplaceAll2.contains(strReplaceAll)) {
                                class793Var = MatchMode.CONTAINS;
                            }
                            if (class793Var == null && Arrays.stream(class817Var.aliases()).anyMatch(alias -> {
                                return alias.replaceAll("\\s+", "").toLowerCase(Locale.ROOT).contains(strReplaceAll);
                            })) {
                                class793Var = MatchMode.ALIAS;
                            }
                            if (class793Var != null) {
                                this.matches.add(new SearchMatch(new FrameSearchTarget(class817Var, class732Var), class793Var));
                            }
                            for (Setting class661Var : class817Var.settings()) {
                                if (class661Var != null) {
                                    MatchMode class793VarMethod012 = matchText(class661Var.getName() != null ? class661Var.getName().effective() : "", class661Var.getDescription() != null ? class661Var.getDescription().effective() : "", strReplaceAll);
                                    if (class793VarMethod012 != null) {
                                        this.matches.add(new SearchMatch(new SettingSearchTarget(class732Var, class817Var, class661Var), class793VarMethod012));
                                    }
                                }
                            }
                        }
                    }
                }
                Collections.sort(this.matches);
            }
            rebuildResultRows();
            this.scrollArea.scrollTo(0.0f);
            this.selectedIndex = this.resultRows.isEmpty() ? -1 : 0;
        });
    }

    @Override
    public void render(DrawCtx class699Var) throws MatchException {
        if (this.openAnimator.isZero()) {
            return;
        }
        ThemePalette class764VarPalette = class699Var.theme().palette();
        ColorStack class115VarColorStack = class699Var.drawEngine().colorStack();
        if (this.searchField.focused()) {
            class699Var.window().interceptKeyboard(true);
        }
        class115VarColorStack.push();
        class115VarColorStack.alpha(this.openAnimator.smoothAnimation());
        class699Var.roundedBlur(Expensive.INSTANCE.windowController().searchBlur().getBlurFramebuffer().getColorAttachment(), x(), y(), width(), height(), cornerRadius, class699Var.colorStack().white());
        class699Var.fillRoundedRect(x() - 1.0f, y(), width() + smallGap, height() + 1.0f, cornerRadius, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb(), 0.9f));
        class699Var.fillOutlinedRoundedRect(this.searchBoxBounds.x(), this.searchBoxBounds.y(), this.searchBoxBounds.width(), this.searchBoxBounds.height() + Math.min(computeContentHeight(), maxHeight), horizontalPadding, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(400).argb()), class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(600).argb()));
        renderSearchBox(class699Var, class764VarPalette, class115VarColorStack);
        if (!this.resultRows.isEmpty()) {
            renderResults(class699Var, class764VarPalette, class115VarColorStack);
        }
        super.render(class699Var);
        class115VarColorStack.pop();
    }

    @Nullable
    public static MatchMode matchText(String str, String str2, String str3) {
        String lowerCase = str.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        String lowerCase2 = str2.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        MatchMode class793Var = null;
        if (lowerCase.equals(str3)) {
            class793Var = MatchMode.EXACT;
        } else if (lowerCase.startsWith(str3)) {
            class793Var = MatchMode.STARTS_WITH;
        } else if (lowerCase.contains(str3) || lowerCase2.contains(str3)) {
            class793Var = MatchMode.CONTAINS;
        }
        return class793Var;
    }

    public void renderSearchBox(DrawCtx class699Var, ThemePalette class764Var, ColorStack class115Var) {
        float fX = this.searchBoxBounds.x() + horizontalPadding;
        int iComputeColor = class115Var.computeColor(class764Var.text().tone(600).argb());
        String strText = this.searchField.text();
        class699Var.textureVerticalC(this.searchIcon, fX, this.searchBoxBounds.y() + (this.searchBoxBounds.height() / smallGap), this.searchIcon.width(), this.searchIcon.height(), iComputeColor);
        float fWidth = ((fX + this.searchIcon.width()) + cornerRadius) - this.searchField.viewportOffset();
        float height = this.font.getHeight(fontSize);
        float fY = (this.searchBoxBounds.y() + (this.searchBoxBounds.height() / smallGap)) - Math.round(height / smallGap);
        class699Var.drawEngine().beginStencil();
        class699Var.fillRect(fX + this.searchIcon.width() + 6.0f, this.searchBoxBounds.y(), this.searchBoxBounds.width() - 40.0f, this.searchBoxBounds.height(), class115Var.white());
        class699Var.drawEngine().prepareStencil(1);
        if (this.searchField.hasSelection()) {
            int iSelMin = this.searchField.selMin();
            int iSelMax = this.searchField.selMax();
            float fTextWidthPhysical = fWidth + class699Var.textWidthPhysical(this.font, strText.substring(0, iSelMin), iconSize);
            class699Var.fillRoundedRect(fTextWidthPhysical, fY - 1.0f, ((fWidth + class699Var.textWidthPhysical(this.font, strText.substring(0, iSelMax), iconSize)) + 1.0f) - fTextWidthPhysical, height + smallGap, 4.0f, class115Var.computeColor(class764Var.accent().argb(), 128));
        }
        if (!strText.isEmpty() && this.searchField.focused()) {
            iComputeColor = class115Var.computeColor(class764Var.text().tone(200).argb());
        }
        class699Var.text(this.font, (!strText.isEmpty() || this.searchField.focused()) ? strText : Lang.SEARCH.effective(), iconSize, fWidth, fY, iComputeColor);
        if (this.searchField.isCursorVisible() && !this.searchField.hasSelection()) {
            class699Var.fillRect(fWidth + class699Var.textWidthPhysical(this.font, strText.substring(0, this.searchField.cursorIndex()), iconSize), fY, 1.0f, height, class115Var.computeColor(16777215));
        }
        class699Var.drawEngine().endStencil();
    }

    public void renderResults(DrawCtx class699Var, ThemePalette class764Var, ColorStack class115Var) throws MatchException {
        float fMethod019 = getContentHeight();
        this.scrollArea.beginArea(class699Var, this.resultsBounds.x(), this.resultsBounds.y(), this.resultsBounds.width(), this.resultsBounds.height());
        float fY = this.resultsBounds.y() + smallGap;
        float fX = this.resultsBounds.x() + horizontalPadding;
        float fWidth = (this.resultsBounds.width() - horizontalPadding) - 25.0f;
        boolean z = true;
        int i = 0;
        for (Map.Entry<String, List<SearchResultRow>> entry : ((Map<String, List<SearchResultRow>>) this.resultRows.stream().collect(Collectors.groupingBy(class792Var -> {
            return class792Var.qualifier().effective();
        }))).entrySet()) {
            String str = (String) entry.getKey();
            List<SearchResultRow> list = entry.getValue();
            if (!z) {
                fY += groupSpacing;
            }
            z = false;
            class699Var.text(this.font, str, 11, fX, fY, class115Var.computeColor(class764Var.text().tone(600).argb()));
            float f = fY + 21.0f;
            for (SearchResultRow class792Var2 : list) {
                if (i == this.selectedIndex) {
                    class699Var.fillOutlinedRoundedRect(fX, f, fWidth, rowHeight, 6.0f, 2.5f, class115Var.computeColor(class764Var.surfaceOutline().tone(300).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(400).argb()));
                }
                if (i < this.clickBehaviors.size()) {
                    float fSmoothAnimation = this.clickBehaviors.get(i).hoverAnimation().smoothAnimation();
                    if (fSmoothAnimation > epsilon) {
                        class699Var.fillRoundedRect(fX - smallGap, f, fWidth + 4.0f, rowHeight, 6.0f, class115Var.computeColor(class764Var.accent().argb(), 0.08f * fSmoothAnimation));
                    }
                }
                SearchTypeBadge class791VarTypeLabel = class792Var2.typeLabel();
                float f2 = fX + cornerRadius;
                class699Var.textureVerticalC(class791VarTypeLabel.texture(), f2, f + 14.0f, class791VarTypeLabel.texture().width(), class791VarTypeLabel.texture().height(), class115Var.computeColor(class764Var.text().tone(500).argb()));
                String strMethod010 = getResultName(class792Var2);
                float fWidth2 = f2 + class791VarTypeLabel.texture().width() + cornerRadius;
                class699Var.text(this.font, strMethod010, iconSize, fWidth2, (f + 14.0f) - (this.font.getHeight(fontSize) / smallGap), class115Var.computeColor(class764Var.text().tone(200).argb()));
                if (class791VarTypeLabel.text() != null) {
                    int iComputeColor = class115Var.computeColor(class764Var.accent().argb());
                    float fTextWidthPhysical = class699Var.textWidthPhysical(this.boldFont, class791VarTypeLabel.text(), 9);
                    float height = this.boldFont.getHeight(9.0f);
                    float f3 = fTextWidthPhysical + groupSpacing;
                    float fTextWidthPhysical2 = fWidth2 + class699Var.textWidthPhysical(this.font, strMethod010, iconSize) + 6.0f;
                    float f4 = ((f + 14.0f) - (15.0f / smallGap)) + 1.0f;
                    class699Var.fillRoundedRect(fTextWidthPhysical2, f4, f3, 15.0f, 5.0f, class115Var.computeColor(class764Var.accent().argb(), 0.1f));
                    class699Var.text(this.boldFont, class791VarTypeLabel.text(), 9, (fTextWidthPhysical2 + (f3 / smallGap)) - (fTextWidthPhysical / smallGap), (f4 + (15.0f / smallGap)) - (height / smallGap), iComputeColor);
                }
                renderResultPath(class699Var, class764Var, class115Var, class792Var2, ((fX + fWidth) - measureResultPath(class699Var, class792Var2)) - cornerRadius, f + 14.0f);
                f += 30.0f;
                i++;
            }
            fY = f - smallGap;
        }
        this.scrollArea.endArea(class699Var, fMethod019);
    }

    public String getResultName(SearchResultRow class792Var) {
        SearchNavTarget class844VarSearchTarget = class792Var.searchResult().searchTarget();
        if (class844VarSearchTarget instanceof SettingSearchTarget) {
            return ((SettingSearchTarget) class844VarSearchTarget).setting().getName().effective();
        }
        SearchNavTarget class844VarSearchTarget2 = class792Var.searchResult().searchTarget();
        if (!(class844VarSearchTarget2 instanceof FrameSearchTarget)) {
            return "";
        }
        AbstractFrame class757VarFrameContainer = ((FrameSearchTarget) class844VarSearchTarget2).frameContainer();
        return class757VarFrameContainer instanceof ModuleCard ? ((ModuleCard) class757VarFrameContainer).name() : "";
    }

    public void renderResultPath(DrawCtx class699Var, ThemePalette class764Var, ColorStack class115Var, SearchResultRow class792Var, float f, float f2) throws MatchException {
        SearchNavTarget class844VarSearchTarget = class792Var.searchResult().searchTarget();
        int iComputeColor = class115Var.computeColor(class764Var.text().tone(500).argb());
        int iComputeColor2 = class115Var.computeColor(class764Var.text().tone(500).argb());
        if (class844VarSearchTarget instanceof SettingSearchTarget) {
            SettingSearchTarget class845Var = (SettingSearchTarget) class844VarSearchTarget;
            GlTexture class073VarIcon = class845Var.tab().icon();
            class073VarIcon.setDimensions(11, 11);
            class699Var.textureVerticalC(class073VarIcon, f, f2, class073VarIcon.width(), class073VarIcon.height(), iComputeColor2);
            float fWidth = f + class073VarIcon.width() + 3.0f;
            String strTitle = class845Var.tab().title();
            class699Var.text(this.font, strTitle, 11, fWidth, f2 - (this.font.getHeight(11.0f) / smallGap), iComputeColor);
            float fTextWidthPhysical = fWidth + class699Var.textWidthPhysical(this.font, strTitle, 11) + smallGap;
            class699Var.textureVerticalC(this.arrowIcon, fTextWidthPhysical, f2 + 1.0f, this.arrowIcon.width(), this.arrowIcon.height(), iComputeColor2);
            float fWidth2 = fTextWidthPhysical + this.arrowIcon.width() + smallGap;
            class699Var.textureVerticalC(this.frameIcon, fWidth2, f2, 11, 11, iComputeColor2);
            float f3 = fWidth2 + 14.0f;
            AbstractFrame class757VarFrame = class845Var.frame();
            if (class757VarFrame instanceof ModuleCard) {
                class699Var.text(this.font, ((ModuleCard) class757VarFrame).name(), 11, f3, f2 - (this.font.getHeight(11.0f) / smallGap), iComputeColor);
                return;
            }
            return;
        }
        if (class844VarSearchTarget instanceof FrameSearchTarget) {
            FrameSearchTarget class842Var = (FrameSearchTarget) class844VarSearchTarget;
            try {
                AbstractFrame class757VarFrameContainer = class842Var.frameContainer();
                MenuTabElement class732VarTab = class842Var.tab();
                GlTexture class073VarIcon2 = class732VarTab.icon();
                class073VarIcon2.setDimensions(11, 11);
                class699Var.textureVerticalC(class073VarIcon2, f, f2, class073VarIcon2.width(), class073VarIcon2.height(), iComputeColor2);
                float fWidth3 = f + class073VarIcon2.width() + 3.0f;
                String strTitle2 = class732VarTab.title();
                class699Var.text(this.font, strTitle2, 11, fWidth3, f2 - (this.font.getHeight(11.0f) / smallGap), iComputeColor);
                float fTextWidthPhysical2 = fWidth3 + class699Var.textWidthPhysical(this.font, strTitle2, 11) + smallGap;
                class699Var.textureVerticalC(this.arrowIcon, fTextWidthPhysical2, f2 + 1.0f, this.arrowIcon.width(), this.arrowIcon.height(), iComputeColor2);
                float fWidth4 = fTextWidthPhysical2 + this.arrowIcon.width() + smallGap;
                if (class757VarFrameContainer instanceof ModuleCard) {
                    ModuleCategory category = ((ModuleCard) class757VarFrameContainer).module().getCategory();
                    GlTexture class073VarIcon3 = category != null ? category.icon() : this.frameIcon;
                    class073VarIcon3.setDimensions(11, 11);
                    class699Var.textureVerticalC(class073VarIcon3, fWidth4, f2, 11, 11, iComputeColor2);
                    class699Var.text(this.font, category != null ? category.displayName() : "Без категории", 11, fWidth4 + 14.0f, f2 - (this.font.getHeight(11.0f) / smallGap), iComputeColor);
                }
            } catch (Throwable th) {
                throw new MatchException(th.toString(), th);
            }
        }
    }

    public float measureResultPath(DrawCtx class699Var, SearchResultRow class792Var) throws MatchException {
        SearchNavTarget class844VarSearchTarget = class792Var.searchResult().searchTarget();
        float fTextWidthPhysical = 0.0f;
        if (class844VarSearchTarget instanceof SettingSearchTarget) {
            SettingSearchTarget class845Var = (SettingSearchTarget) class844VarSearchTarget;
            fTextWidthPhysical = 0.0f + 14.0f + class699Var.textWidthPhysical(this.font, class845Var.tab().title(), 11) + smallGap + this.arrowIcon.width() + smallGap + 14.0f;
            AbstractFrame class757VarFrame = class845Var.frame();
            if (class757VarFrame instanceof ModuleCard) {
                fTextWidthPhysical += class699Var.textWidthPhysical(this.font, ((ModuleCard) class757VarFrame).name(), 11);
            }
        } else if (class844VarSearchTarget instanceof FrameSearchTarget) {
            FrameSearchTarget class842Var = (FrameSearchTarget) class844VarSearchTarget;
            try {
                AbstractFrame class757VarFrameContainer = class842Var.frameContainer();
                fTextWidthPhysical = 0.0f + 14.0f + class699Var.textWidthPhysical(this.font, class842Var.tab().title(), 11) + smallGap + this.arrowIcon.width() + smallGap;
                if (class757VarFrameContainer instanceof ModuleCard) {
                    ModuleCategory category = ((ModuleCard) class757VarFrameContainer).module().getCategory();
                    fTextWidthPhysical = fTextWidthPhysical + 14.0f + class699Var.textWidthPhysical(this.font, category != null ? category.displayName() : "Без категории", 11);
                }
            } catch (Throwable th) {
                throw new MatchException(th.toString(), th);
            }
        }
        return fTextWidthPhysical;
    }

    @Override
    public void layout(LayoutScaleContext class698Var) {
        if (this.openAnimator.isZero()) {
            return;
        }
        this.searchBoxBounds.withPosition((x() + (width() / smallGap)) - 300.0f, y() + 50.0f).withSize(600.0f, 36.0f);
        this.resultsBounds.withPosition(this.searchBoxBounds.x(), this.searchBoxBounds.y() + this.searchBoxBounds.height()).withSize(this.searchBoxBounds.width(), Math.min(computeContentHeight(), maxHeight));
        this.scrollbar.setSize(4.0f, this.resultsBounds.height());
        this.scrollbar.setPosition(((this.searchBoxBounds.x() + this.searchBoxBounds.width()) - horizontalPadding) - this.scrollbar.width(), this.resultsBounds.y());
        this.searchIcon.setDimensions(iconSize, iconSize);
        this.frameIcon.setDimensions(12, 12);
        this.multiEnumIcon.setDimensions(12, 12);
        this.sliderIcon.setDimensions(12, 12);
        this.enumIcon.setDimensions(12, 12);
        this.gamepadIcon.setDimensions(12, 12);
        this.groupIcon.setDimensions(12, 12);
        this.arrowIcon.setDimensions(9, 8);
        this.clickIcon.setDimensions(12, 12);
        this.brushIcon.setDimensions(12, 12);
        this.textIcon.setDimensions(12, 12);
        this.searchField.listen(this.searchBoxBounds.x(), this.searchBoxBounds.y(), this.searchBoxBounds.width(), this.searchBoxBounds.height(), this.searchBoxBounds.width() - 47.0f, this.searchBoxBounds.x() + horizontalPadding + this.searchIcon.width() + cornerRadius, class698Var.scaleFactor());
        rebuildClickBehaviors();
        super.layout(class698Var);
    }

    public void rebuildClickBehaviors() {
        this.clickBehaviors.clear();
        float fY = this.resultsBounds.y() + smallGap;
        float fX = this.resultsBounds.x() + horizontalPadding;
        float fWidth = (this.resultsBounds.width() - horizontalPadding) - 25.0f;
        boolean z = true;
        int i = 0;
        Iterator<Map.Entry<String, List<SearchResultRow>>> it = ((Map<String, List<SearchResultRow>>) this.resultRows.stream().collect(Collectors.groupingBy(class792Var -> {
            return class792Var.qualifier().effective();
        }))).entrySet().iterator();
        while (it.hasNext()) {
            List<SearchResultRow> list = (List) ((Map.Entry) it.next()).getValue();
            if (!z) {
                fY += groupSpacing;
            }
            z = false;
            float f = fY + 21.0f;
            for (SearchResultRow class792Var2 : list) {
                ClickableBehavior class766Var = new ClickableBehavior();
                int i2 = i;
                class766Var.clickCallback(() -> {
                    selectResult(i2);
                });
                class766Var.setDimensions(fX, f, fWidth, rowHeight);
                this.clickBehaviors.add(class766Var);
                f += 30.0f;
                i++;
            }
            fY = f - smallGap;
        }
    }

    @Override
    public void animation(WeightedEngine class141Var) {
        this.openAnimator.animate(class141Var);
        this.selectionAnimator.animate(class141Var);
        this.searchField.animate(class141Var);
        Iterator<ClickableBehavior> it = this.clickBehaviors.iterator();
        while (it.hasNext()) {
            it.next().animate(class141Var);
        }
        super.animation(class141Var);
    }

    @Override
    public boolean handleInput(InputEventContext class688Var, boolean z) throws MatchException {
        InputEventContext class688VarWithMouseOffset = class688Var.withMouseOffset(0.0f, this.scrollArea.scrollY());
        if (this.openAnimator.isZero() && !this.opened) {
            return false;
        }
        if (super.handleInput(class688Var, z)) {
            return true;
        }
        if ((class688Var.inputEvent() instanceof ScrollInput) && !z) {
            return true;
        }
        if (!z) {
            InputEvent class691VarInputEvent = class688VarWithMouseOffset.inputEvent();
            if (class691VarInputEvent instanceof KeyInput) {
                KeyInput class696Var = (KeyInput) class691VarInputEvent;
                if (class696Var.keyAction().press() || class696Var.keyAction().repeat()) {
                    switch (class696Var.keyCode()) {
                        case 256:
                            close();
                            return true;
                        case 257:
                            if (this.selectedIndex >= 0 && this.selectedIndex < this.resultRows.size()) {
                                selectResult(this.selectedIndex);
                                return true;
                            }
                            break;
                        case 264:
                            if (!this.resultRows.isEmpty()) {
                                this.keyboardNavigation = true;
                                this.selectedIndex = Math.min(this.selectedIndex + 1, this.resultRows.size() - 1);
                                scrollToSelected();
                                return true;
                            }
                            break;
                        case 265:
                            if (!this.resultRows.isEmpty()) {
                                this.keyboardNavigation = true;
                                this.selectedIndex = Math.max(this.selectedIndex - 1, 0);
                                scrollToSelected();
                                return true;
                            }
                            break;
                    }
                }
            }
            InputEvent class691VarInputEvent2 = class688VarWithMouseOffset.inputEvent();
            if (class691VarInputEvent2 instanceof CursorMoveInput) {
                try {
                    PixelPoint class708VarMousePosition = ((CursorMoveInput) class691VarInputEvent2).mousePosition();
                    float fX = class708VarMousePosition.x();
                    float fY = class708VarMousePosition.y();
                    boolean z2 = Float.isNaN(this.lastMouseX) || Math.abs(fX - this.lastMouseX) > epsilon || Math.abs(fY - this.lastMouseY) > epsilon;
                    this.lastMouseX = fX;
                    this.lastMouseY = fY;
                    if (z2) {
                        this.keyboardNavigation = false;
                        for (int i = 0; i < this.clickBehaviors.size(); i++) {
                            ClickableBehavior class766Var = this.clickBehaviors.get(i);
                            if (class688VarWithMouseOffset.inArea(class766Var.ownerX(), class766Var.ownerY(), class766Var.ownerW(), class766Var.ownerH())) {
                                this.selectedIndex = i;
                            }
                        }
                    }
                } catch (Throwable th) {
                    throw new MatchException(th.toString(), th);
                }
            }
        }
        Iterator<ClickableBehavior> it = this.clickBehaviors.iterator();
        while (it.hasNext()) {
            if (it.next().handleInput(class688VarWithMouseOffset, z)) {
                return true;
            }
        }
        if (this.searchField.handleInput(class688VarWithMouseOffset, z)) {
            return true;
        }
        InputEvent class691VarInputEvent3 = class688VarWithMouseOffset.inputEvent();
        if (!(class691VarInputEvent3 instanceof MouseButtonInput)) {
            return false;
        }
        MouseButtonInput class693Var = (MouseButtonInput) class691VarInputEvent3;
        if (!class693Var.action().press() || class693Var.button() != 0) {
            return false;
        }
        if (class688VarWithMouseOffset.inArea(this.searchBoxBounds.x(), this.searchBoxBounds.y(), this.searchBoxBounds.width(), this.searchBoxBounds.height() + Math.min(computeContentHeight(), maxHeight))) {
            return true;
        }
        close();
        this.searchField.focused(false);
        return true;
    }

    public void scrollToSelected() {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.resultRows.size()) {
            return;
        }
        float fMethod014 = getRowY(this.selectedIndex);
        float fScrollY = this.scrollArea.scrollY();
        float fY = this.resultsBounds.y() + cornerRadius;
        float fY2 = (this.resultsBounds.y() + this.resultsBounds.height()) - cornerRadius;
        float f = fMethod014 - fScrollY;
        float f2 = f + rowHeight;
        if (f < fY) {
            this.scrollArea.scrollTo((fMethod014 - this.resultsBounds.y()) - cornerRadius);
        } else if (f2 > fY2) {
            this.scrollArea.scrollTo(fScrollY + (f2 - fY2));
        }
    }

    public float getRowY(int i) {
        float fY = this.resultsBounds.y() + smallGap;
        boolean z = true;
        int i2 = 0;
        for (List<SearchResultRow> list : ((Map<String, List<SearchResultRow>>) this.resultRows.stream().collect(Collectors.groupingBy(class792Var -> {
            return class792Var.qualifier().effective();
        }))).values()) {
            if (!z) {
                fY += groupSpacing;
            }
            z = false;
            float f = fY + 21.0f;
            for (int i3 = 0; i3 < list.size(); i3++) {
                if (i2 == i) {
                    return f;
                }
                f += 30.0f;
                i2++;
            }
            fY = f - smallGap;
        }
        return fY;
    }

    public void selectResult(int i) throws MatchException {
        SearchResultRow class792VarMethod006 = getRow(i);
        if (class792VarMethod006 == null) {
            return;
        }
        SearchNavTarget class844VarSearchTarget = class792VarMethod006.searchResult().searchTarget();
        if (class844VarSearchTarget instanceof SettingSearchTarget class845Var) {
            this.navigator.focusSetting(class845Var.tab(), class845Var.frame(), class845Var.setting());
        } else if (class844VarSearchTarget instanceof FrameSearchTarget class842Var) {
            this.navigator.focusFrame(class842Var.frameContainer(), class842Var.tab());
        }
        Expensive.INSTANCE.menuWindow().collapseExpandedHeader();
        close();
    }

    public boolean isFocused() {
        return this.searchField.focused();
    }

    public SearchResultRow getRow(int i) {
        if (i < 0) {
            return null;
        }
        int i2 = 0;
        Iterator<List<SearchResultRow>> it = ((Map<String, List<SearchResultRow>>) this.resultRows.stream().collect(Collectors.groupingBy(class792Var -> {
            return class792Var.qualifier().effective();
        }))).values().iterator();
        while (it.hasNext()) {
            for (SearchResultRow class792Var2 : it.next()) {
                if (i2 == i) {
                    return class792Var2;
                }
                i2++;
            }
        }
        return null;
    }

    public void open() {
        this.opened = true;
        visible(true);
        this.openAnimator.state(true);
        this.searchField.focused(true);
        if (this.resultRows.isEmpty()) {
            return;
        }
        this.selectedIndex = 0;
    }

    public void close() {
        this.opened = false;
        this.openAnimator.state(false);
        this.searchField.focused(false);
        this.selectedIndex = -1;
        this.searchField.clearText();
    }

    public void invert() {
        this.opened = !this.opened;
        this.openAnimator.state(this.opened);
        this.searchField.focused(this.opened);
        if (!this.opened) {
            this.searchField.clearText();
        }
        if (this.opened && !this.resultRows.isEmpty()) {
            this.selectedIndex = 0;
        } else {
            if (this.opened) {
                return;
            }
            this.selectedIndex = -1;
        }
    }

    public void rebuildResultRows() {
        this.resultRows.clear();
        for (SearchMatch class794Var : this.matches) {
            this.resultRows.add(new SearchResultRow(class794Var, class794Var.searchTarget() instanceof SettingSearchTarget ? Translation.clearText("Настройки") : Translation.clearText("Модули"), createTypeBadge(class794Var)));
        }
    }

    public SearchTypeBadge createTypeBadge(SearchMatch class794Var) {
        SearchNavTarget class844VarSearchTarget = class794Var.searchTarget();
        if (!(class844VarSearchTarget instanceof SettingSearchTarget)) {
            return new SearchTypeBadge(null, this.frameIcon);
        }
        Setting class661Var = ((SettingSearchTarget) class844VarSearchTarget).setting();
        Objects.requireNonNull(class661Var);
        switch (typeSwitch02448(class661Var)) {
            case 0:
                return new SearchTypeBadge("CHECKBOX", this.multiEnumIcon);
            case 1:
                return new SearchTypeBadge("SLIDER", this.sliderIcon);
            case 2:
                return new SearchTypeBadge("GROUP", this.groupIcon);
            case 3:
                return new SearchTypeBadge("KEYBIND", this.gamepadIcon);
            case 4:
                return new SearchTypeBadge("MULTI COMBOBOX", this.enumIcon);
            case 5:
                return new SearchTypeBadge("TEXTFIELD", this.textIcon);
            case 6:
                return new SearchTypeBadge("COMBOBOX", this.enumIcon);
            case 7:
                return new SearchTypeBadge("BUTTON", this.clickIcon);
            case 8:
                return new SearchTypeBadge("COLOR", this.brushIcon);
            default:
                return new SearchTypeBadge(null, this.frameIcon);
        }
    }

    static int typeSwitch02448(Object o) {
        if (o == null) return -1;
        if (o instanceof BooleanSetting) return 0;
        if (o instanceof NumberSetting) return 1;
        if (o instanceof ExpandableSetting) return 2;
        if (o instanceof KeybindSetting) return 3;
        if (o instanceof MultiSelectSetting) return 4;
        if (o instanceof TextFieldSetting) return 5;
        if (o instanceof ModeSetting) return 6;
        if (o instanceof ButtonSetting) return 7;
        if (o instanceof ColorSetting) return 8;
        return 9;
    }

    public float computeContentHeight() {
        if (this.resultRows.isEmpty()) {
            return 0.0f;
        }
        float fMax = 2.0f;
        boolean z = true;
        for (List<SearchResultRow> list : ((Map<String, List<SearchResultRow>>) this.resultRows.stream().collect(Collectors.groupingBy(class792Var -> {
            return class792Var.qualifier().effective();
        }))).values()) {
            if (!z) {
                fMax += groupSpacing;
            }
            z = false;
            int size = list.size();
            fMax = fMax + 21.0f + (size * rowHeight) + (Math.max(0, size - 1) * smallGap);
        }
        return fMax + horizontalPadding;
    }

    public float getContentHeight() {
        return computeContentHeight();
    }

    public boolean isOpened() {
        return this.opened;
    }
}
