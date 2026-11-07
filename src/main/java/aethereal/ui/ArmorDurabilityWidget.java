package aethereal.ui;
import aethereal.model.DragRenderContext;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.model.Translation;
import aethereal.render.ColorStack;
import aethereal.render.Fonts;
import aethereal.render.MsdfFont;
import aethereal.render.ScreenResolution;
import aethereal.render.ThemePalette;
import aethereal.type.ArmorDisplayMode;
import aethereal.type.ArmorLayoutMode;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.DrawEngine;
import aethereal.util.WeightedEngine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArmorDurabilityWidget extends Draggable {
    static final float padding = 6.0f;
    static final float iconSize = 16.0f;
    static final float rowHeight = 18.0f;
    static final float gap = 3.0f;
    static final float cornerRadius = 8.0f;
    static final float barHeight = 2.0f;
    static final float itemScale = 0.5f;
    static final float textSize = 9.0f;
    static final float textThickness = 0.05f;

    public final ModeSetting<ArmorDisplayMode> displayMode;
    public final ModeSetting<ArmorLayoutMode> layoutMode;
    public final NumberSetting scale;
    public final NumberSetting opacity;
    public final NumberSetting backgroundOpacity;

    public float width;
    public float height;

    public ArmorDurabilityWidget(BooleanSupplier booleanSupplier) {
        super("Armor Durability", booleanSupplier);
        this.displayMode = new ModeSetting(Translation.clearText("Display"), Translation.clearText("Show durability as percent or value")).values(ArmorDisplayMode.class).select(ArmorDisplayMode.PERCENT);
        this.layoutMode = new ModeSetting(Translation.clearText("Layout"), Translation.clearText("Vertical or horizontal arrangement")).values(ArmorLayoutMode.class).select(ArmorLayoutMode.VERTICAL);
        this.scale = new NumberSetting(Translation.clearText("Scale")).range(50.0f, 200.0f).step(5.0f).currentValue(100.0f).unit(SettingUnit.PERCENTS);
        this.opacity = new NumberSetting(Translation.clearText("Opacity")).range(0.0f, 100.0f).step(5.0f).currentValue(100.0f).unit(SettingUnit.PERCENTS);
        this.backgroundOpacity = new NumberSetting(Translation.clearText("Background")).range(0.0f, 100.0f).step(5.0f).currentValue(60.0f).unit(SettingUnit.PERCENTS);
        addSettings(this.displayMode, this.layoutMode, this.scale, this.opacity, this.backgroundOpacity);
        this.width = 60.0f;
        this.height = 40.0f;
        this.x = 20.0f;
        this.y = 120.0f;
    }

    public float scale() {
        return this.scale.currentValue() / 100.0f;
    }

    @Override
    public void layout(DragRenderContext class809Var) {
    }

    public List<ItemStack> collectArmor() {
        ArrayList<ItemStack> arrayList = new ArrayList<>();
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return arrayList;
        }
        List<ItemStack> armor = player.getInventory().armor;
        for (int i = armor.size() - 1; i >= 0; i--) {
            ItemStack stack = armor.get(i);
            if (stack.isEmpty() || stack.getItem() == Items.ELYTRA) {
                continue;
            }
            arrayList.add(stack);
        }
        return arrayList;
    }

    public String durabilityText(ItemStack stack, boolean percent) {
        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return percent ? "100%" : "0";
        }
        int remaining = stack.getMaxDamage() - stack.getDamage();
        if (remaining < 0) {
            remaining = 0;
        }
        if (!percent) {
            return String.valueOf(remaining);
        }
        return Math.round((remaining * 100.0f) / stack.getMaxDamage()) + "%";
    }

    public static float durabilityRatio(ItemStack stack) {
        if (!stack.isDamageable() || stack.getMaxDamage() <= 0) {
            return 1.0f;
        }
        float f = ((float) (stack.getMaxDamage() - stack.getDamage())) / stack.getMaxDamage();
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    public int durabilityColor(ColorStack colorStack, ItemStack stack, float alpha) {
        float ratio = durabilityRatio(stack);
        if (ratio >= 0.5f) {
            return colorStack.computeColor(0xFF5AE05A, alpha);
        }
        if (ratio >= 0.25f) {
            return colorStack.computeColor(0xFFFFC040, alpha);
        }
        return colorStack.computeColor(0xFFFF5050, alpha);
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (!isVisible() || class809Var.resolution() == null || class809Var.resolution().screenWidth() <= 0 || class809Var.resolution().screenHeight() <= 0) {
            return;
        }
        List<ItemStack> items = collectArmor();
        if (items.isEmpty()) {
            this.width = 0.0f;
            this.height = 0.0f;
            return;
        }
        float s = scale();
        float alpha = this.opacity.currentValue() / 100.0f;
        float bgAlpha = (this.backgroundOpacity.currentValue() / 100.0f) * alpha;
        boolean percent = this.displayMode.currentValue() == ArmorDisplayMode.PERCENT;
        boolean horizontal = this.layoutMode.currentValue() == ArmorLayoutMode.HORIZONTAL;

        MatrixStack matrixStack = class809Var.matrixStack();
        DrawEngine engine = class809Var.drawEngine();
        ColorStack colorStack = engine.colorStack();
        ThemePalette palette = class809Var.theme().palette();
        MsdfFont font = Fonts.INTER_SEMIBOLD.get();

        float fPad = padding * s;
        float fIcon = iconSize * s;
        float fGap = gap * s;
        float fBar = barHeight * s;
        float fRow = rowHeight * s;
        float fFont = textSize * s;
        float textHeight = font.getHeight(fFont);

        float maxText = 0.0f;
        for (ItemStack stack : items) {
            maxText = Math.max(maxText, font.getWidth(durabilityText(stack, percent), fFont));
        }

        if (!horizontal) {
            this.width = (fPad * 2.0f) + fIcon + fGap + maxText;
            this.height = (fPad * 2.0f) + (items.size() * fRow) + ((items.size() - 1) * fGap);
        } else {
            float entryWidth = Math.max(fIcon, maxText);
            float entryHeight = fIcon + fGap + textHeight + fBar + (fGap * 0.5f);
            this.width = (fPad * 2.0f) + (items.size() * entryWidth) + ((items.size() - 1) * fGap);
            this.height = (fPad * 2.0f) + entryHeight;
        }

        int outline = colorStack.computeColor(palette.surfaceOutline().tone(600).argb(), alpha);
        int fillTop = colorStack.computeColor(palette.surfaceBackground().tone(801).argb(), bgAlpha);
        int fillBottom = colorStack.computeColor(palette.surfaceBackground().tone(900).argb(), bgAlpha);
        engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, cornerRadius * s, 2.0f * s, outline, fillTop, fillTop, fillBottom, fillBottom);

        if (!horizontal) {
            float entryWidth = this.width - (fPad * 2.0f);
            float cursorY = this.y + fPad;
            for (ItemStack stack : items) {
                float iconX = this.x + fPad;
                float iconY = cursorY + ((fRow - fIcon) / 2.0f);
                engine.itemStack(matrixStack.peek().getPositionMatrix(), stack, iconX, iconY, itemScale * s, alpha);

                String text = durabilityText(stack, percent);
                float textY = cursorY + ((fRow - textHeight) / 2.0f);
                engine.msdfFont(matrixStack.peek().getPositionMatrix(), font, text, this.x + fPad + fIcon + fGap, textY, fFont, textThickness, colorStack.computeColor(palette.text().tone(100).argb(), alpha));

                float barX = this.x + fPad;
                float barY = cursorY + fRow - fBar;
                engine.rectangle(matrixStack.peek().getPositionMatrix(), barX, barY, entryWidth, fBar, colorStack.computeColor(palette.surfaceBackground().tone(300).argb(), 0.8f * alpha));
                float ratio = durabilityRatio(stack);
                if (ratio > 0.0f) {
                    engine.rectangle(matrixStack.peek().getPositionMatrix(), barX, barY, entryWidth * ratio, fBar, durabilityColor(colorStack, stack, alpha));
                }
                cursorY += fRow + fGap;
            }
        } else {
            float entryWidth = Math.max(fIcon, maxText);
            float cursorX = this.x + fPad;
            for (ItemStack stack : items) {
                String text = durabilityText(stack, percent);
                float textWidth = font.getWidth(text, fFont);
                float iconX = cursorX + ((entryWidth - fIcon) / 2.0f);
                engine.itemStack(matrixStack.peek().getPositionMatrix(), stack, iconX, this.y + fPad, itemScale * s, alpha);

                float textY = this.y + fPad + fIcon + fGap;
                engine.msdfFont(matrixStack.peek().getPositionMatrix(), font, text, cursorX + ((entryWidth - textWidth) / 2.0f), textY, fFont, textThickness, colorStack.computeColor(palette.text().tone(100).argb(), alpha));

                float barY = textY + textHeight + (fGap * 0.5f);
                engine.rectangle(matrixStack.peek().getPositionMatrix(), cursorX, barY, entryWidth, fBar, colorStack.computeColor(palette.surfaceBackground().tone(300).argb(), 0.8f * alpha));
                float ratio = durabilityRatio(stack);
                if (ratio > 0.0f) {
                    engine.rectangle(matrixStack.peek().getPositionMatrix(), cursorX, barY, entryWidth * ratio, fBar, durabilityColor(colorStack, stack, alpha));
                }
                cursorX += entryWidth + fGap;
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
    }

    @Override
    public void update() {
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
    public boolean hasTooltip() {
        return true;
    }
}
