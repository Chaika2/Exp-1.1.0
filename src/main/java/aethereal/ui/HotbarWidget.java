package aethereal.ui;
import aethereal.Lang;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.render.Fonts;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.render.ScreenResolution;
import aethereal.render.ThemePalette;
import aethereal.util.MathUtil;
import aethereal.util.WeightedEngine;
import aethereal.type.SettingUnit;
import aethereal.ui.setting.NumberSetting;

import java.util.function.BooleanSupplier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class HotbarWidget extends Draggable {
    static final float slotSize = 22.0f;
    static final float cornerRadius = 6.0f;
    static final float panelRadius = 8.0f;
    static final float offhandGap = 12.0f;
    static final int countFontSize = 7;
    static final int numberFontSize = 7;
    static final float itemScale = 0.40625f;
    static final float itemOffset = 4.6f;
    public float width;
    public float height;
    public boolean pinned;
    public final NumberSetting size;

    public HotbarWidget(BooleanSupplier booleanSupplier) {
        super("Hotbar", booleanSupplier);
        this.width = slotSize * 9.0f;
        this.height = slotSize;
        this.pinned = true;
        this.size = new NumberSetting(Lang.WIDGET_HOTBAR_SIZE).range(50.0f, 200.0f).step(5.0f).currentValue(100.0f).unit(SettingUnit.PERCENTS);
        addSettings(this.size);
        this.x = 240.0f;
        this.y = 100.0f;
    }

    public float scale() {
        return this.size.currentValue() / 100.0f;
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        ScreenResolution res = class809Var.resolution();
        if (this.pinned && res != null) {
            this.x = (res.screenWidth() / 2.0f) - (this.width / 2.0f);
            this.y = res.screenHeight() - this.height - 16.0f;
        }
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (!isVisible() || Mc.INSTANCE.getPlayer() == null || class809Var.resolution() == null || class809Var.resolution().screenWidth() <= 0 || class809Var.resolution().screenHeight() <= 0) {
            return;
        }
        float s = scale();
        MatrixStack matrixStack = class809Var.matrixStack();
        DrawEngine engine = class809Var.drawEngine();
        ColorStack colorStack = engine.colorStack();
        ThemePalette palette = class809Var.theme().palette();
        PlayerInventory inventory = Mc.INSTANCE.getPlayer().getInventory();
        int selectedSlot = inventory.selectedSlot;
        float fSlot = slotSize * s;
        float fOffhandGap = offhandGap * s;
        float fCorner = cornerRadius * s;
        float fPanel = panelRadius * s;
        this.width = fSlot * 9.0f;
        this.height = fSlot;

        boolean zOffhandVisible = !inventory.offHand.get(0).isEmpty();
        float fPanelX = this.x;

        int iOutline = colorStack.computeColor(palette.surfaceOutline().tone(600).argb());
        int iPanelBg = colorStack.computeColor(palette.surfaceBackground().tone(900).argb());
        int iPanelBgAlt = colorStack.computeColor(palette.surfaceBackground().tone(801).argb());
        engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fPanelX, this.y, this.width, this.height, fPanel, 2.5f * s, iOutline, iPanelBgAlt, iPanelBgAlt, iPanelBg, iPanelBg);

        float fSlotX = fPanelX;
        for (int i = 0; i < 9; i++) {
            drawSlot(matrixStack, engine, colorStack, palette, inventory.main.get(i), i, selectedSlot, fSlotX, this.y, i == selectedSlot, s);
            fSlotX += fSlot;
        }

        if (zOffhandVisible) {
            float fOffhandX = fPanelX - fSlot - fOffhandGap;
            engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fOffhandX, this.y, fSlot, fSlot, fCorner, 2.5f * s, iOutline, iPanelBgAlt, iPanelBgAlt, iPanelBg, iPanelBg);
            drawSlot(matrixStack, engine, colorStack, palette, inventory.offHand.get(0), -1, selectedSlot, fOffhandX, this.y, false, s);
        }
    }

    public void drawSlot(MatrixStack matrixStack, DrawEngine engine, ColorStack colorStack, ThemePalette palette, ItemStack stack, int index, int selectedSlot, float x, float y, boolean selected, float s) {
        MsdfFont numberFont = Fonts.INTER_EXTRA_BOLD.get();
        MsdfFont countFont = Fonts.INTER_SEMIBOLD.get();
        float fSlot = slotSize * s;
        float fCorner = cornerRadius * s;
        int iSlotBg;
        int iSlotOutline;
        if (selected) {
            iSlotBg = colorStack.computeColor(palette.accent().argb(), 0.22f);
            iSlotOutline = colorStack.computeColor(palette.accent().argb(), 0.65f);
        } else {
            iSlotBg = colorStack.computeColor(palette.surfaceBackground().tone(800).argb(), 0.5f);
            iSlotOutline = colorStack.computeColor(palette.surfaceOutline().tone(500).argb(), 0.5f);
        }
        engine.roundedRectangle(matrixStack.peek().getPositionMatrix(), x + 1.0f * s, y + 1.0f * s, 20.0f * s, 20.0f * s, fCorner, 1.5f * s, iSlotOutline, iSlotBg, iSlotBg, iSlotBg, iSlotBg);

        if (stack.isEmpty()) {
            if (index >= 0) {
                String strIndex = String.valueOf(index + 1);
                engine.msdfFont(matrixStack.peek().getPositionMatrix(), numberFont, strIndex, (float) Math.floor(x + ((fSlot - numberFont.getWidth(strIndex, numberFontSize * s)) / 2.0f)), (float) Math.floor(y + ((fSlot - numberFont.getHeight(numberFontSize * s)) / 2.0f)), numberFontSize * s, 0.0f, colorStack.computeColor(palette.text().tone(500).argb(), 0.5f));
            }
            return;
        }

        float fItemX = x + itemOffset * s;
        float fItemY = y + itemOffset * s;
        engine.itemStack(matrixStack.peek().getPositionMatrix(), stack, fItemX, fItemY, itemScale * s, 1.0f);

        if (stack.isDamageable() && stack.getDamage() > 0 && stack.getMaxDamage() > 0) {
            float fDura = ((float) (stack.getMaxDamage() - stack.getDamage())) / ((float) stack.getMaxDamage());
            float fBarW = fSlot - 6.0f * s;
            float fBarX = x + 3.0f * s;
            float fBarY = (y + fSlot) - 2.5f * s;
            int iBarBg = colorStack.computeColor(palette.surfaceBackground().tone(300).argb(), 0.8f);
            int iBarColor;
            if (fDura >= 0.5f) {
                iBarColor = colorStack.computeColor(0xFF5AE05A, 1.0f);
            } else if (fDura >= 0.25f) {
                iBarColor = colorStack.computeColor(0xFFFFC040, 1.0f);
            } else {
                iBarColor = colorStack.computeColor(0xFFFF5050, 1.0f);
            }
            engine.rectangle(matrixStack.peek().getPositionMatrix(), fBarX, fBarY, fBarW, 1.5f * s, iBarBg);
            if (fDura > 0.0f) {
                engine.rectangle(matrixStack.peek().getPositionMatrix(), fBarX, fBarY, fBarW * fDura, 1.5f * s, iBarColor);
            }
        }

        if (stack.getCount() > 1) {
            String strCount = "x" + stack.getCount();
            engine.msdfFont(matrixStack.peek().getPositionMatrix(), countFont, strCount, x + fSlot - countFont.getWidth(strCount, countFontSize * s) - 3.0f * s, ((y + fSlot) - countFont.getHeight(countFontSize * s)) - 2.5f * s, countFontSize * s, 0.0f, colorStack.computeColor(palette.text().tone(100).argb()));
        }

        if (index >= 0) {
            float fCooldown = Mc.INSTANCE.getPlayer().getItemCooldownManager().getCooldownProgress(stack, Mc.INSTANCE.getMinecraft().getRenderTickCounter().getTickDelta(false));
            if (fCooldown > 0.0f) {
                float fClamp = MathUtil.clamp(fCooldown, 0.0f, 1.0f);
                float fArcX = (x + fSlot) - 5.0f * s;
                float fArcY = y + (fSlot / 2.0f);
                int iArcBg = colorStack.computeColor(palette.surfaceBackground().tone(300).argb());
                int iArcFill = colorStack.computeColor(ThemePalette.darkGolden.argb());
                engine.arc(matrixStack.peek().getPositionMatrix(), fArcX, fArcY, 5.0f * s, 0.0f, 360.0f, 2.0f * s, iArcBg);
                float fStart = 270.0f - (360.0f * fClamp);
                if (fStart >= 0.0f) {
                    engine.arc(matrixStack.peek().getPositionMatrix(), fArcX, fArcY, 5.0f * s, fStart, 270.0f, 2.0f * s, iArcFill);
                } else {
                    engine.arc(matrixStack.peek().getPositionMatrix(), fArcX, fArcY, 5.0f * s, 360.0f + fStart, 360.0f, 2.0f * s, iArcFill);
                    engine.arc(matrixStack.peek().getPositionMatrix(), fArcX, fArcY, 5.0f * s, 0.0f, 270.0f, 2.0f * s, iArcFill);
                }
            }
        }
    }

    @Override
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        if (class807Var.isLeftButtonPressed() && class807Var.press() && z && !class807Var.intercepted()) {
            this.pinned = false;
        }
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
}