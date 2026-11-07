package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.module.FTHelperModule;
import aethereal.render.Fonts;
import aethereal.module.HWHelperModule;
import aethereal.model.ItemBindEntry;
import aethereal.type.ItemSearchRule;
import aethereal.ui.setting.KeybindSetting;
import aethereal.util.KeyboardUtil;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.render.ScreenResolution;
import aethereal.render.ThemePalette;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public class ItemBindWidget extends Draggable {
    static final float countBadgeOffset = 13.0f;
    static final float padding = 6.0f;
    static final float sectionGap = 8.0f;
    public final Map<KeybindSetting, ItemBindEntry> ftEntryMap;
    public final List<ItemBindEntry> ftEntries;
    public final AnimatedFloat ftAnimation;
    public final Map<KeybindSetting, ItemBindEntry> hwEntryMap;
    public final List<ItemBindEntry> hwEntries;
    public final AnimatedFloat hwAnimation;
    public float width;
    public float height;
    public float ftWidth;
    public float ftHeight;
    public float hwWidth;
    public float hwHeight;

    public ItemBindWidget(BooleanSupplier booleanSupplier) {
        super("ItemBind", booleanSupplier);
        this.ftEntryMap = new LinkedHashMap();
        this.ftEntries = new ArrayList();
        this.ftAnimation = new AnimatedFloat(250, Easings.LINEAR);
        this.hwEntryMap = new LinkedHashMap();
        this.hwEntries = new ArrayList();
        this.hwAnimation = new AnimatedFloat(250, Easings.LINEAR);
        if (this.resolution != null) {
            this.x = this.resolution.screenWidth() / 2.0f;
            this.y = this.resolution.screenHeight() - 100.0f;
        }
    }

    @Override
    public void layout(DragRenderContext class809Var) {
    }

    @Override
    public void draw(DragRenderContext class809Var) {
        ScreenResolution class710VarResolution = class809Var.resolution();
        if (class710VarResolution == null || class710VarResolution.screenWidth() <= 0 || class710VarResolution.screenHeight() <= 0 || !isVisible()) {
            return;
        }
        render(class809Var);
    }

    @Override
    public void render(DragRenderContext class809Var) {
        boolean z = !this.ftAnimation.isZero();
        boolean z2 = !this.hwAnimation.isZero();
        if (isVisible()) {
            if (z || z2) {
                MsdfFont class161Var = Fonts.INTER_EXTRA_BOLD.get();
                this.ftWidth = computeRowWidth(this.ftEntries, class161Var, this.ftAnimation.animatedValue());
                this.ftHeight = (!z || this.ftWidth <= 0.0f) ? 0.0f : 23.0f;
                this.hwWidth = computeRowWidth(this.hwEntries, class161Var, this.hwAnimation.animatedValue());
                this.hwHeight = (!z2 || this.hwWidth <= 0.0f) ? 0.0f : 23.0f;
                this.width = Math.max(this.ftWidth, this.hwWidth);
                this.height = this.ftHeight + this.hwHeight + ((this.ftHeight > 0.0f ? 1 : (this.ftHeight == 0.0f ? 0 : -1)) > 0 && (this.hwHeight > 0.0f ? 1 : (this.hwHeight == 0.0f ? 0 : -1)) > 0 ? sectionGap : 0.0f);
                MatrixStack matrixStack = class809Var.matrixStack();
                DrawEngine class154VarDrawEngine = class809Var.drawEngine();
                ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                ThemePalette class764VarPalette = Expensive.INSTANCE.theme().palette();
                matrixStack.push();
                matrixStack.translate(this.x, this.y, 0.0f);
                float f = 0.0f;
                if (z && this.ftHeight > 0.0f) {
                    drawRow(matrixStack, class154VarDrawEngine, class115VarColorStack, class764VarPalette, class161Var, this.ftEntries, this.ftAnimation.animatedValue(), (this.width - this.ftWidth) / 2.0f, 0.0f, 23.0f);
                    f = 0.0f + this.ftHeight + (this.hwHeight > 0.0f ? sectionGap : 0.0f);
                }
                if (z2 && this.hwHeight > 0.0f) {
                    drawRow(matrixStack, class154VarDrawEngine, class115VarColorStack, class764VarPalette, class161Var, this.hwEntries, this.hwAnimation.animatedValue(), (this.width - this.hwWidth) / 2.0f, f, 23.0f);
                }
                matrixStack.pop();
            }
        }
    }

    public float computeRowWidth(List<ItemBindEntry> list, MsdfFont class161Var, float f) {
        float width = 0.0f;
        for (ItemBindEntry class643Var : list) {
            boolean z = Mc.INSTANCE.getPlayer().getItemCooldownManager().getCooldownProgress(class643Var.stack, Mc.INSTANCE.getMinecraft().getRenderTickCounter().getTickDelta(false)) > 0.0f;
            if (class643Var.animation.animatedValue() * f > 0.01f) {
                width += 25.0f + class161Var.getWidth(class643Var.keyText, 10.0f) + (z ? 15.0f : 0.0f) + padding + padding;
            }
        }
        if (width > 0.0f) {
            width -= padding;
        }
        return width;
    }

    public void drawRow(MatrixStack matrixStack, DrawEngine class154Var, ColorStack class115Var, ThemePalette class764Var, MsdfFont class161Var, List<ItemBindEntry> list, float f, float f2, float f3, float f4) {
        float f5 = f2;
        MsdfFont class161Var2 = Fonts.INTER_EXTRA_BOLD.get();
        for (ItemBindEntry class643Var : list) {
            float fAnimatedValue = class643Var.animation.animatedValue() * f;
            if (fAnimatedValue > 0.01f) {
                float cooldownProgress = Mc.INSTANCE.getPlayer().getItemCooldownManager().getCooldownProgress(class643Var.stack, Mc.INSTANCE.getMinecraft().getRenderTickCounter().getTickDelta(false));
                boolean z = cooldownProgress > 0.0f;
                class115Var.push();
                class115Var.alpha(fAnimatedValue);
                String str = class643Var.keyText;
                float width = 25.0f + class161Var.getWidth(str, 10.0f) + (z ? 15.0f : 0.0f) + padding;
                float f6 = f5;
                class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), f6, f3, width, f4, 7.0f, 2.5f, class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
                class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), f6 + 2.0f, f3 + 2.0f, 19.0f, 19.0f, padding, z ? class115Var.computeColor(15316036, 0.25f) : class115Var.computeColor(class764Var.accent().argb(), 0.25f));
                float f7 = ((f6 + 2.0f) + 9.5f) - 6.5f;
                float f8 = ((f3 + 2.0f) + 9.5f) - 6.5f;
                class154Var.itemStack(matrixStack.peek().getPositionMatrix(), class643Var.stack, f7, f8, 0.40625f, 1.0f);
                if (class643Var.count > 1) {
                    String strValueOf = String.valueOf(class643Var.count);
                    class154Var.msdfFont(matrixStack.peek().getPositionMatrix(), class161Var, strValueOf, (f7 + countBadgeOffset) - class161Var2.getWidth(strValueOf, 7.0f), ((f8 + countBadgeOffset) - class161Var2.getHeight(7.0f)) + 1.0f, 7.0f, 0.0f, class115Var.computeColor(class764Var.text().tone(100).argb()));
                }
                class154Var.msdfFont(matrixStack.peek().getPositionMatrix(), class161Var, str, f6 + 2.0f + 19.0f + 4.0f, (f3 + (f4 / 2.0f)) - (class161Var.getHeight(10.0f) / 2.0f), 10.0f, 0.0f, class115Var.computeColor(class764Var.text().tone(300).argb()));
                if (z) {
                    float fClamp = MathUtil.clamp(cooldownProgress, 0.0f, 1.0f);
                    float f9 = ((f6 + width) - padding) - 5.0f;
                    float f10 = f3 + (f4 / 2.0f);
                    int iComputeColor = class115Var.computeColor(class764Var.surfaceBackground().tone(300).argb());
                    int iComputeColor2 = class115Var.computeColor(ThemePalette.darkGolden.argb());
                    class154Var.arc(matrixStack.peek().getPositionMatrix(), f9, f10, 5.0f, 0.0f, 360.0f, 2.0f, iComputeColor);
                    float f11 = 270.0f - (360.0f * fClamp);
                    if (f11 >= 0.0f) {
                        class154Var.arc(matrixStack.peek().getPositionMatrix(), f9, f10, 5.0f, f11, 270.0f, 2.0f, iComputeColor2);
                    } else {
                        class154Var.arc(matrixStack.peek().getPositionMatrix(), f9, f10, 5.0f, 360.0f + f11, 360.0f, 2.0f, iComputeColor2);
                        class154Var.arc(matrixStack.peek().getPositionMatrix(), f9, f10, 5.0f, 0.0f, 270.0f, 2.0f, iComputeColor2);
                    }
                }
                f5 += width + padding;
                class115Var.pop();
            }
        }
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
    public void animate(WeightedEngine class141Var) {
        this.ftEntries.forEach(class643Var -> {
            class643Var.animate(class141Var);
        });
        this.hwEntries.forEach(class643Var2 -> {
            class643Var2.animate(class141Var);
        });
        this.ftAnimation.animate(class141Var);
        this.hwAnimation.animate(class141Var);
    }

    public ItemStack findMatchingStack(DefaultedList<ItemStack> defaultedList, ItemStack itemStack, Predicate<ItemStack> predicate) {
        for (ItemStack itemStack2 : defaultedList) {
            if (!itemStack2.isEmpty() && predicate.test(itemStack2)) {
                return itemStack2;
            }
        }
        return (itemStack.isEmpty() || !predicate.test(itemStack)) ? ItemStack.EMPTY : itemStack;
    }

    @Override
    public void update() {
        ClientPlayerEntity player;
        if (isVisible()) {
            Mc class815Var = Mc.INSTANCE;
            if (class815Var.isWorldLoaded() && (player = class815Var.getPlayer()) != null) {
                FTHelperModule class492Var = (FTHelperModule) Expensive.INSTANCE.moduleRepository().get(FTHelperModule.class);
                HWHelperModule class498Var = (HWHelperModule) Expensive.INSTANCE.moduleRepository().get(HWHelperModule.class);
                boolean z = class492Var != null && class492Var.isState();
                boolean z2 = class498Var != null && class498Var.isState();
                if (z) {
                    updateEntries(player, class815Var, class492Var.getBindItems(), this.ftEntryMap, this.ftEntries, this.ftAnimation);
                } else {
                    this.ftEntries.clear();
                    this.ftAnimation.destination(0.0f);
                }
                if (z2) {
                    updateEntries(player, class815Var, class498Var.getBindItems(), this.hwEntryMap, this.hwEntries, this.hwAnimation);
                } else {
                    this.hwEntries.clear();
                    this.hwAnimation.destination(0.0f);
                }
            }
        }
    }

    public void updateEntries(ClientPlayerEntity clientPlayerEntity, Mc class815Var, Map<KeybindSetting, ? extends ItemSearchRule> map, Map<KeybindSetting, ItemBindEntry> map2, List<ItemBindEntry> list, AnimatedFloat class042Var) {
        ItemBindEntry class643Var;
        if (!map.keySet().stream().anyMatch(class663Var -> {
            return class663Var.getKey() != -1;
        })) {
            list.clear();
            map2.values().forEach(class643Var2 -> {
                class643Var2.active = false;
            });
            class042Var.destination(0.0f);
            return;
        }
        HashSet hashSet = new HashSet();
        boolean z = class815Var.getCurrentScreen() instanceof ChatScreen;
        boolean z2 = false;
        for (Map.Entry<KeybindSetting, ? extends ItemSearchRule> entry : map.entrySet()) {
            KeybindSetting key = entry.getKey();
            if (key.getKey() != -1) {
                ItemSearchRule value = entry.getValue();
                ItemBindEntry class643VarComputeIfAbsent = map2.computeIfAbsent(key, class663Var2 -> {
                    return new ItemBindEntry();
                });
                ItemStack itemStackMethod007 = findMatchingStack(clientPlayerEntity.getInventory().main, clientPlayerEntity.getOffHandStack(), value.getSearchPredicate());
                if (itemStackMethod007.isEmpty()) {
                    class643VarComputeIfAbsent.active = false;
                } else {
                    class643VarComputeIfAbsent.stack = itemStackMethod007.copy();
                    class643VarComputeIfAbsent.count = itemStackMethod007.getCount();
                    class643VarComputeIfAbsent.keyText = KeyboardUtil.formatCombination(key.getKeyBind());
                    class643VarComputeIfAbsent.active = true;
                    hashSet.add(key);
                    z2 = true;
                }
            }
        }
        if (!z2 && z) {
            int i = 0;
            for (Map.Entry<KeybindSetting, ? extends ItemSearchRule> entry2 : map.entrySet()) {
                KeybindSetting key2 = entry2.getKey();
                if (key2.getKey() != -1) {
                    if (i >= 4) {
                        break;
                    }
                    ItemSearchRule value2 = entry2.getValue();
                    ItemBindEntry class643Var3 = map2.get(key2);
                    if (class643Var3 != null) {
                        class643Var3.stack = value2.boundItem().getDefaultStack();
                        class643Var3.count = 0;
                        class643Var3.keyText = KeyboardUtil.formatCombination(key2.getKeyBind());
                        class643Var3.active = true;
                        hashSet.add(key2);
                        i++;
                    }
                }
            }
        }
        map2.forEach((class663Var3, class643Var4) -> {
            if (hashSet.contains(class663Var3)) {
                return;
            }
            class643Var4.active = false;
        });
        list.clear();
        for (KeybindSetting class663Var4 : map.keySet()) {
            if (class663Var4.getKey() != -1 && (class643Var = map2.get(class663Var4)) != null && (class643Var.active || !class643Var.animation.isZero())) {
                list.add(class643Var);
            }
        }
        class042Var.destination(!list.isEmpty() ? 1.0f : 0.0f);
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return this.height;
    }
}
