package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.ui.setting.BooleanSetting;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.ui.setting.ExpandableSetting;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.type.HotkeyCategory;
import aethereal.model.HotkeyEntry;
import aethereal.util.KeyboardUtil;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.module.Module;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.Setting;
import aethereal.render.ThemePalette;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;

public class HotkeysWidget extends Draggable {
    public final MsdfFont semiBoldFont;
    public final MsdfFont boldFont;
    public final GlTexture starsTexture;
    public final GlTexture buttonsIcon;
    public final GlTexture frameIcon;
    public final AnimatedFloat opacityAnimation;
    public final List<HotkeyEntry> entries;
    public final Map<String, HotkeyEntry> entryMap;
    public final WidgetBounds headerBounds;
    public final MultiSelectSetting<HotkeyCategory> hiddenCategoriesSetting;
    public float width;
    public float height;

    public HotkeysWidget(BooleanSupplier booleanSupplier) {
        super("Hotkeys", booleanSupplier);
        this.semiBoldFont = Fonts.INTER_SEMIBOLD.get();
        this.boldFont = Fonts.INTER_EXTRA_BOLD.get();
        this.starsTexture = new GlTexture(new ClasspathResource("/textures/stars.png"));
        this.buttonsIcon = new GlTexture(new ClasspathResource("/icons/menu/new/buttons.png"));
        this.frameIcon = new GlTexture(new ClasspathResource("/icons/menu/new/frame.png"));
        this.opacityAnimation = new AnimatedFloat(200, Easings.LINEAR);
        this.entries = new ArrayList();
        this.entryMap = new HashMap();
        this.headerBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 19.0f);
        this.hiddenCategoriesSetting = new MultiSelectSetting(Lang.WIDGET_HOTKEYS_HIDDEN_CATEGORIES).values(HotkeyCategory.class);
        this.width = 161.0f;
        this.height = 31.0f;
        this.x = 10.0f;
        this.y = 49.0f;
        addSettings(this.hiddenCategoriesSetting);
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
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        return false;
    }

    @Override
    public boolean cursor(MouseMoveInput class808Var, boolean z) {
        return z && !class808Var.intercepted();
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        if (isVisible()) {
            float fMax = 150.0f;
            float fMin = 39.0f;
            float fMax2 = 0.0f;
            int i = -1;
            for (int i2 = 0; i2 < this.entries.size(); i2++) {
                if (this.entries.get(i2).anim.smoothAnimation() > 0.001f) {
                    i = i2;
                }
            }
            int i3 = 0;
            while (i3 < this.entries.size()) {
                HotkeyEntry class640Var = this.entries.get(i3);
                float fMin2 = Math.min(class640Var.anim.smoothAnimation(), 1.0f);
                String combination = KeyboardUtil.formatCombination(class640Var.keys);
                float width = this.semiBoldFont.getWidth(class640Var.name, 12.0f);
                float width2 = 12.0f + this.boldFont.getWidth(combination, 10.0f);
                float height = class640Var.hasModule() ? 22.0f + this.semiBoldFont.getHeight(12.0f) : this.boldFont.getHeight(10.0f) + 6.0f;
                fMax = Math.max(fMax, 150.0f + ((((width + width2) + 60.0f) - 150.0f) * fMin2));
                fMin += Math.min(height, height * fMin2) + (i3 != i ? 6.0f * fMin2 : 0.0f);
                fMax2 = Math.max(fMax2, fMin2);
                i3++;
            }
            if (fMax2 > 0.0f) {
                fMin += 10.0f * fMax2;
            }
            this.width = fMax;
            this.height = fMin;
            this.headerBounds.withSize(fMax - 20.0f, 19.0f).withPosition(this.x + 10.0f, (this.y + 19.5f) - 9.5f);
        }
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (!isVisible() || this.opacityAnimation.isZero()) {
            return;
        }
        DrawEngine class154VarDrawEngine = class809Var.drawEngine();
        MatrixStack matrixStack = class809Var.matrixStack();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        ThemePalette class764VarPalette = class809Var.theme().palette();
        float fAnimatedValue = this.opacityAnimation.animatedValue();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, 8.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iComputeColor2, iComputeColor2, iComputeColor, iComputeColor);
        class154VarDrawEngine.texture(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, class154VarDrawEngine.bindTexture(this.starsTexture.textureWithSTB()), class115VarColorStack.white());
        class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.semiBoldFont, "Hotkeys", this.headerBounds.x(), (this.headerBounds.y() + 9.5f) - (this.semiBoldFont.getHeight(13.0f) / 2.0f), 13.0f, 0.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()));
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.headerBounds.right() - 19.0f, this.headerBounds.y(), 19.0f, 19.0f, 6.0f, class115VarColorStack.computeColor(class764VarPalette.accentBright().argb(), 0.1f));
        class154VarDrawEngine.textureVerticalCHorizontalC(matrixStack.peek().getPositionMatrix(), this.buttonsIcon, this.headerBounds.right() - 9.5f, this.headerBounds.y() + 9.5f, 11, 11, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
        float fBottom = this.headerBounds.bottom() + 12.0f;
        for (HotkeyEntry class640Var : this.entries) {
            String combination = KeyboardUtil.formatCombination(class640Var.keys);
            float fMin = Math.min(class640Var.anim.smoothAnimation(), 1.0f);
            float width = this.boldFont.getWidth(combination, 10.0f);
            float height = this.boldFont.getHeight(10.0f) + 6.0f;
            float f = 12.0f + width;
            float f2 = this.x + (10.0f * fAnimatedValue * fMin);
            float height2 = class640Var.hasModule() ? 22.0f + this.semiBoldFont.getHeight(12.0f) : height;
            float fRight = this.headerBounds.right() - f;
            float f3 = class640Var.hasModule() ? (fBottom + (height2 / 2.0f)) - (height / 2.0f) : fBottom;
            float height3 = class640Var.hasModule() ? fBottom + 17.0f : (fBottom + (height / 2.0f)) - (this.semiBoldFont.getHeight(12.0f) / 2.0f);
            class115VarColorStack.push();
            class115VarColorStack.alpha(fMin);
            if (class640Var.hasModule() && class640Var.moduleName != null) {
                float height4 = this.boldFont.getHeight(8.0f) + 4.0f;
                class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), f2, fBottom, 20.0f + this.boldFont.getWidth(class640Var.moduleName.toUpperCase(), 8.0f), height4, 4.0f, class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 0.1f));
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.frameIcon, f2 + 4.0f, fBottom + (height4 / 2.0f), 9, 9, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.boldFont, class640Var.moduleName.toUpperCase(), f2 + 16.0f, (fBottom + (height4 / 2.0f)) - (this.boldFont.getHeight(8.0f) / 2.0f), 8.0f, 0.05f, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
            }
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.semiBoldFont, class640Var.name, f2, height3, 12.0f, 0.05f, class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb()));
            class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fRight, f3, f, height, 6.0f, class115VarColorStack.computeColor(1974050, 0.5f));
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.boldFont, combination, (fRight + (f / 2.0f)) - (width / 2.0f), (f3 + (height / 2.0f)) - (this.boldFont.getHeight(10.0f) / 2.0f), 10.0f, 0.05f, class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb()));
            class115VarColorStack.pop();
            fBottom += (height2 + 6.0f) * fMin;
        }
        class115VarColorStack.pop();
    }

    @Override
    public void animate(WeightedEngine class141Var) {
        if (Mc.INSTANCE.getPlayer() == null) {
            return;
        }
        this.entries.forEach(class640Var -> {
            class640Var.anim.animate(class141Var);
        });
        this.opacityAnimation.animate(class141Var);
    }

    @Override
    public void update() {
        if (isVisible()) {
            HashSet hashSet = new HashSet();
            boolean zMethod007 = false;
            for (Module class605Var : Expensive.INSTANCE.moduleRepository().getModules()) {
                HotkeyCategory class641VarMethod002 = HotkeyCategory.fromTab(class605Var.getModuleTab());
                if (class641VarMethod002 == null || !this.hiddenCategoriesSetting.isSelected(class641VarMethod002)) {
                    if (class605Var.hasKeyBind()) {
                        zMethod007 |= updateEntry(hashSet, "m:" + class605Var.getName(), () -> {
                            return new HotkeyEntry(class605Var.getName(), class605Var.getKeyBind());
                        }, class605Var.getKeyBind(), class605Var.isState());
                    }
                    if (class605Var.isState()) {
                        for (Setting class661Var : class605Var.getSettings()) {
                            if (class661Var instanceof BooleanSetting) {
                                BooleanSetting class665Var = (BooleanSetting) class661Var;
                                if (class665Var.getKey() != -1) {
                                    zMethod007 |= updateEntry(hashSet, "s:" + class605Var.getName() + ":" + class665Var.getName().effective(), () -> {
                                        return new HotkeyEntry(class605Var, class665Var.getName().effective(), class665Var.getKeyBind());
                                    }, class665Var.getKeyBind(), class665Var.isValue());
                                }
                            }
                            if (class661Var instanceof ExpandableSetting) {
                                ExpandableSetting class670Var = (ExpandableSetting) class661Var;
                                String strEffective = class670Var.getName().effective();
                                if (class670Var.getKey() != -1) {
                                    zMethod007 |= updateEntry(hashSet, "s:" + class605Var.getName() + ":" + strEffective, () -> {
                                        return new HotkeyEntry(class605Var, strEffective, class670Var.getKeyBind());
                                    }, class670Var.getKeyBind(), class670Var.isValue());
                                }
                                for (Setting class661Var2 : class670Var.getSubSettings()) {
                                    if (class661Var2 instanceof BooleanSetting) {
                                        BooleanSetting class665Var2 = (BooleanSetting) class661Var2;
                                        if (class665Var2.getKey() != -1) {
                                            zMethod007 |= updateEntry(hashSet, "s:" + class605Var.getName() + ":" + strEffective + "/" + class665Var2.getName().effective(), () -> {
                                                return new HotkeyEntry(class605Var, strEffective + "/" + class665Var2.getName().effective(), class665Var2.getKeyBind());
                                            }, class665Var2.getKeyBind(), class665Var2.isValue());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            this.entryMap.keySet().retainAll(hashSet);
            this.entries.clear();
            Stream<HotkeyEntry> streamSorted = this.entryMap.values().stream().sorted(Comparator.comparing(class640Var -> {
                return class640Var.name;
            }, String.CASE_INSENSITIVE_ORDER));
            List<HotkeyEntry> list = this.entries;
            Objects.requireNonNull(list);
            streamSorted.forEach((v1) -> {
                list.add(v1);
            });
            this.opacityAnimation.destination(((this.entries.isEmpty() || !zMethod007) && !(Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen)) ? 0.0f : 1.0f);
        }
    }

    public boolean updateEntry(Set<String> set, String str, Supplier<HotkeyEntry> supplier, List<Integer> list, boolean z) {
        set.add(str);
        HotkeyEntry class640VarComputeIfAbsent = this.entryMap.computeIfAbsent(str, str2 -> {
            return (HotkeyEntry) supplier.get();
        });
        class640VarComputeIfAbsent.keys.clear();
        if (list != null) {
            class640VarComputeIfAbsent.keys.addAll(list);
        }
        class640VarComputeIfAbsent.anim.state(z);
        return z;
    }
}
