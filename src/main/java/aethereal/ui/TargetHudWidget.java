package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.module.AttackAuraModule;

import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.type.ItemSpriteManager;
import aethereal.type.TargetHudStyle;
import aethereal.util.IteratorUtil;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.type.SettingUnit;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.module.NameProtectModule;
import aethereal.type.HudWidgetType;
import aethereal.util.ScoreboardHelper;
import aethereal.util.ServerUtil;
import aethereal.util.StencilBufferUtil;
import aethereal.render.ThemePalette;
import aethereal.render.ToggleAnimator;
import aethereal.model.UvBounds;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetHudWidget extends Draggable {
    public float width;
    public float height;
    public final AnimatedFloat fadeAnimation;
    public final ToggleAnimator toggleAnimatorA;
    public final ToggleAnimator toggleAnimatorB;
    public final AnimatedFloat healthAnimation;
    public final AnimatedFloat absorptionAnimation;
    public final GlTexture backgroundTexture;
    public final GlTexture heartTexture;
    public final GlTexture gappleTexture;
    public final MsdfFont nameFont;
    public final MsdfFont valueFont;
    public LivingEntity target;

    public final WidgetBounds avatarBounds;
    public final WidgetBounds contentBounds;
    public final WidgetBounds itemsBounds;
    public final WidgetBounds nameBounds;
    static final float padding = 10.0f;
    static final float cornerRadius = 8.0f;
    static final float itemSize = 12.0f;
    static final float itemSpacing = 3.0f;
    static final int iconSize = 10;
    static final float halfDivisor = 2.0f;
    static final float sectionGap = 4.0f;
    static final float minWidth = 180.0f;
    public final BooleanSetting showInRaycast;
    public final ModeSetting<TargetHudStyle> style;
    public final NumberSetting size;
    static final int spacingTwo = 2;

    static final double roundIncrement = 0.5d;

    static final double raycastExpand = 0.7d;

    public TargetHudWidget(BooleanSupplier booleanSupplier) {
        super("TargetHud", booleanSupplier);
        this.width = 200.0f;
        this.height = 70.0f;
        this.fadeAnimation = new AnimatedFloat(200, Easings.LINEAR);
        this.toggleAnimatorA = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
        this.toggleAnimatorB = new ToggleAnimator(200, Easings.EASE_IN_OUT_CUBIC);
        this.healthAnimation = new AnimatedFloat(160, Easings.LINEAR);
        this.absorptionAnimation = new AnimatedFloat(180, Easings.LINEAR);
        this.backgroundTexture = new GlTexture(new ClasspathResource("/textures/hud_background.png"));
        this.heartTexture = new GlTexture(new ClasspathResource("/icons/menu/new/heartpulse.png"));
        this.gappleTexture = new GlTexture(new ClasspathResource("/icons/menu/new/gapple.png"));
        this.nameFont = Fonts.INTER_SEMIBOLD.get();
        this.valueFont = Fonts.INTER_BOLD.get();
        this.avatarBounds = new WidgetBounds(0.0f, 0.0f, 50.0f, 50.0f);
        this.contentBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 50.0f);
        this.itemsBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.nameBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 0.0f);
        this.showInRaycast = new BooleanSetting(Lang.WIDGET_TARGETHUD_SHOW_IN_RAYCAST);
        this.style = new ModeSetting(Lang.WIDGET_TARGETHUD_STYLE).values(TargetHudStyle.class);
        this.size = new NumberSetting(Lang.WIDGET_TARGETHUD_SIZE).range(50.0f, 200.0f).step(5.0f).currentValue(100.0f).unit(SettingUnit.PERCENTS);
        addSettings(this.style, this.size, this.showInRaycast);
        this.x = 604.0f;
        this.y = 469.0f;
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        if (isVisible()) {
            TargetHudStyle targetHudStyle = this.style.currentValue();
            if (targetHudStyle == TargetHudStyle.LARGE) {
                layoutLarge(class809Var);
            } else if (targetHudStyle == TargetHudStyle.ZENITH) {
                layoutZenith(class809Var);
            } else if (targetHudStyle == TargetHudStyle.NURS) {
                layoutNurs(class809Var);
            } else {
                layoutCelestial(class809Var);
            }
            if (!this.dragging) {
                var class710Var = class809Var.resolution();
                if (class710Var != null && class710Var.screenWidth() > 0 && class710Var.screenHeight() > 0) {
                    float fMaxX = Math.max(0.0f, class710Var.screenWidth() - width());
                    float fMaxY = Math.max(0.0f, class710Var.screenHeight() - height());
                    if (this.x < 0.0f || this.y < 0.0f || this.x > fMaxX || this.y > fMaxY) {
                        this.x = MathUtil.clamp(this.x, 0.0f, fMaxX);
                        this.y = MathUtil.clamp(this.y, 0.0f, fMaxY);
                    }
                }
            }
        }
    }

    public void layoutLarge(DragRenderContext class809Var) {
        float s = this.scale();
        float fMax;
        List<ItemStack> listMethod009 = getEquipment(this.target);
        boolean z = !listMethod009.isEmpty();
        float f = 50.0f * s;
        if (f != this.height) {
            this.y -= (f - this.height) / halfDivisor;
            this.height = f;
        }
        float fMax2 = Math.max(padding * s, this.height - 20.0f * s);
        this.avatarBounds.withSize(fMax2, fMax2).withPosition(this.x + padding * s, (this.y + (this.height / halfDivisor)) - (fMax2 / halfDivisor));
        this.contentBounds.withPosition(this.avatarBounds.right() + cornerRadius * s, this.avatarBounds.y()).withSize(((width() - 20.0f * s) - this.avatarBounds.width()) - cornerRadius * s, this.avatarBounds.height());
        float size = z ? (listMethod009.size() * itemSize * s) + ((listMethod009.size() - 1) * itemSpacing * s) : 0.0f;
        float fMin = Math.min(this.nameFont.getWidth(getDisplayName(this.target), 13.0f * s), Math.max(0.0f, (160.0f * s - fMax2) - cornerRadius * s));
        float fMax3 = Math.max(this.valueFont.getWidth("20.0", padding * s), this.valueFont.getWidth("1000.0", padding * s));
        float width = this.valueFont.getWidth("20.0", padding * s);
        float f2 = fMax3 + halfDivisor * s + padding * s;
        float f3 = width + halfDivisor * s + padding * s;
        boolean zMethod012 = shouldShowAbsorption(this.target);
        if (z) {
            fMax = Math.max(f2, f3);
        } else {
            fMax = f2 + (zMethod012 ? sectionGap * s + f3 : 0.0f);
        }
        float fWidth = padding * s + this.avatarBounds.width() + cornerRadius * s + Math.max(size, fMin) + cornerRadius * s + fMax + padding * s;
        this.width = z ? Math.max(minWidth * s, fWidth) : fWidth;
    }

    public void layoutZenith(DragRenderContext class809Var) {
        float s = this.scale();
        float f = 50.0f * s;
        if (f != this.height) {
            this.y -= (f - this.height) / halfDivisor;
            this.height = f;
        }
        this.width = 180.0f * s;
        float fHead = 28.0f * s;
        float fX = this.x + 9.0f * s;
        float fY = this.y + 11.0f * s;
        this.avatarBounds.withSize(fHead, fHead).withPosition(fX, fY);
        float fContentX = fX + fHead + 18.0f * s;
        float fRingLeft = (this.x + width()) - 10.0f * s - 30.0f * s;
        float fContentW = Math.max(0.0f, fRingLeft - fContentX - 6.0f * s);
        this.contentBounds.withPosition(fContentX, fY).withSize(fContentW, 28.0f * s);
        this.nameBounds.withPosition(fContentX, this.y + 9.0f * s).withSize(fContentW, this.nameFont.getHeight(12.0f * s));
        this.itemsBounds.withPosition(fContentX, this.y + 34.0f * s).withSize(0.0f, 12.0f * s);
    }

    public void layoutNurs(DragRenderContext class809Var) {
        float s = this.scale();
        float f = 50.0f * s;
        if (f != this.height) {
            this.y -= (f - this.height) / halfDivisor;
            this.height = f;
        }
        this.width = 180.0f * s;
        float fHead = 30.0f * s;
        this.avatarBounds.withSize(fHead, fHead).withPosition(this.x + 10.0f * s, this.y + 10.0f * s);
        this.contentBounds.withPosition(this.x + 48.0f * s, this.y + 8.0f * s).withSize(122.0f * s, 34.0f * s);
        this.nameBounds.withPosition(this.contentBounds.x(), this.contentBounds.y()).withSize(76.0f * s, this.nameFont.getHeight(11.0f * s));
        this.itemsBounds.withPosition(this.contentBounds.x(), this.y + 37.0f * s).withSize(0.0f, 12.0f * s);
    }

    public void layoutCelestial(DragRenderContext class809Var) {
        float s = this.scale();
        float f = 50.0f * s;
        if (f != this.height) {
            this.y -= (f - this.height) / halfDivisor;
            this.height = f;
        }
        this.width = 180.0f * s;
        float fHead = 36.0f * s;
        this.avatarBounds.withSize(fHead, fHead).withPosition(this.x + 8.0f * s, this.y + ((this.height - fHead) / halfDivisor));
        this.contentBounds.withPosition(this.x + 52.0f * s, this.y + 6.0f * s).withSize(this.width - 60.0f * s, 40.0f * s);
        this.nameBounds.withPosition(this.x + 52.0f * s, this.y + 7.0f * s).withSize(this.width - 60.0f * s, this.nameFont.getHeight(12.0f * s));
        List<ItemStack> equipment = getEquipment(this.target);
        float itemW = 13.0f * s;
        float itemGap = 2.0f * s;
        float labelH = Fonts.INTER_SEMIBOLD.get().getHeight(itemLabelFontSize(s));
        float itemsW = 0.0f;
        for (int i = 0; i < equipment.size(); i++) {
            itemsW += itemW + itemCountWidth(equipment.get(i), s);
            if (i < equipment.size() - 1) {
                itemsW += itemGap;
            }
        }
        float rowH = itemW + labelH + 2.0f * s;
        float rowY = this.y - rowH - 2.0f * s;
        this.itemsBounds.withPosition(this.x + this.width - itemsW, rowY).withSize(itemsW, rowH);
    }

    @Override
    public void render(DragRenderContext class809Var) {
        if (isVisible()) {
            float fAnimatedValue = this.fadeAnimation.animatedValue();
            if (this.fadeAnimation.isZero() || this.target == null) {
                return;
            }
            DrawEngine class154VarDrawEngine = class809Var.drawEngine();
            MatrixStack matrixStack = class809Var.matrixStack();
            ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
            ThemePalette class764VarPalette = class809Var.theme().palette();
            TargetHudStyle targetHudStyle = this.style.currentValue();
            if (targetHudStyle == TargetHudStyle.LARGE) {
                renderLarge(class809Var, class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, fAnimatedValue);
            } else if (targetHudStyle == TargetHudStyle.ZENITH) {
                renderZenith(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, fAnimatedValue);
            } else if (targetHudStyle == TargetHudStyle.NURS) {
                renderNurs(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, fAnimatedValue);
            } else {
                renderCelestial(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, fAnimatedValue);
            }
        }
    }

    public void renderLarge(DragRenderContext class809Var, DrawEngine class154VarDrawEngine, MatrixStack matrixStack, ColorStack class115VarColorStack, ThemePalette class764VarPalette, float fAnimatedValue) {
        float s = this.scale();
        List<ItemStack> listMethod009 = getEquipment(this.target);
        boolean z = !listMethod009.isEmpty();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        int iComputeColor = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
        int iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, width(), height(), cornerRadius * s, 2.5f * s, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iComputeColor2, iComputeColor2, iComputeColor, iComputeColor);
        class154VarDrawEngine.texture(matrixStack.peek().getPositionMatrix(), this.x, this.y + padding * s, width(), Math.max(0.0f, height() - 20.0f * s), class154VarDrawEngine.bindTexture(this.backgroundTexture.textureWithSTB()), class115VarColorStack.white());
        renderAvatar(class154VarDrawEngine, matrixStack.peek().getPositionMatrix(), class115VarColorStack, this.avatarBounds.x(), this.avatarBounds.y(), this.avatarBounds.width(), this.avatarBounds.height(), (z ? cornerRadius : 6.0f) * s);
        if (z) {
            float itemW = (listMethod009.size() * itemSize + ((listMethod009.size() - 1) * itemSpacing)) * s;
            float fX = this.contentBounds.right() - itemW;
            float fY = this.contentBounds.y();
            for (ItemStack itemStack : listMethod009) {
                renderWidgetItem(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, itemStack, fX, fY, itemSize * s, 6.5f * s);
                fX += (itemSize + itemSpacing) * s;
            }
            this.itemsBounds.withSize(itemW, itemSize * s).withPosition(this.contentBounds.right() - itemW, this.contentBounds.y());
        } else {
            this.itemsBounds.withSize(0.0f, 0.0f).withPosition(this.contentBounds.x(), this.contentBounds.y());
        }
        String strMethod008 = getDisplayName(this.target);
        float fMax = Math.max(this.valueFont.getWidth("20.0", padding * s), this.valueFont.getWidth("1000.0", padding * s));
        float width = this.valueFont.getWidth("20.0", padding * s);
        float f = fMax + halfDivisor * s + padding * s;
        float f2 = width + halfDivisor * s + padding * s;
        float fWidth = z ? (this.contentBounds.width() - this.itemsBounds.width() - 4.0f * s) : this.contentBounds.width() - (f + (shouldShowAbsorption(this.target) ? sectionGap * s + f2 : 0.0f));
        float fMin = Math.min(this.nameFont.getWidth(strMethod008, 13.0f * s), fWidth);
        this.nameBounds.withPosition(this.contentBounds.x(), this.contentBounds.y()).withSize(fMin, this.nameFont.getHeight(13.0f * s));
        int iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb());
        if (this.nameFont.getWidth(strMethod008, 13.0f * s) > fWidth) {
            class154VarDrawEngine.msdfFontWithHorizontalGradient(matrixStack.peek().getPositionMatrix(), this.nameFont, truncateToWidth(this.nameFont, strMethod008, 13, fWidth / s), this.nameBounds.x(), this.nameBounds.y(), 13.0f * s, 0.05f, iComputeColor3, iComputeColor3 & 16777215);
        } else {
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.nameFont, strMethod008, this.nameBounds.x(), this.nameBounds.y(), 13.0f * s, 0.05f, iComputeColor3);
        }
        float realHealth = ScoreboardHelper.INSTANCE.getRealHealth(this.target);
        float fMethod007 = getEffectiveMaxHealth(this.target);
        float f3 = fMethod007 > 0.0f ? realHealth / fMethod007 : 0.0f;
        this.absorptionAnimation.destination(MathUtil.clamp(this.target.getAbsorptionAmount() / 20.0f, 0.0f, 1.0f));
        this.healthAnimation.destination(MathUtil.clamp(f3, 0.0f, 1.0f));
        int iHp = healthColor(class115VarColorStack, this.healthAnimation.animatedValue());
        boolean zMethod012 = shouldShowAbsorption(this.target);
        float fX2 = this.contentBounds.x();
        float fWidth2 = this.contentBounds.width() - sectionGap * s;
        float fBottom = this.contentBounds.bottom() - 5.0f * s;
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fX2, fBottom, fWidth2, itemSpacing * s, halfDivisor * s, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()));
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fX2, fBottom, fWidth2 * this.healthAnimation.animatedValue(), itemSpacing * s, halfDivisor * s, iHp);
        if (zMethod012) {
            class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fX2, fBottom, fWidth2 * this.absorptionAnimation.animatedValue(), itemSpacing * s, halfDivisor * s, class115VarColorStack.computeColor(ThemePalette.darkGolden.argb()));
        }
        float fAnimatedValue2 = this.healthAnimation.animatedValue() * getEffectiveMaxHealth(this.target);
        float fAnimatedValue3 = this.absorptionAnimation.animatedValue() * (this.target.getMaxAbsorption() != 0.0f ? this.target.getMaxAbsorption() : 20.0f);
        String strMethod002 = formatHp(this.target, fAnimatedValue2);
        String strMethod003 = (!zMethod012 || fAnimatedValue3 <= 0.0f) ? "" : formatValue(fAnimatedValue3);
        if (z) {
            float f4 = fX2 + fWidth2;
            float fY2 = fBottom - this.valueFont.getHeight(padding * s) - 2.0f * s;
            float width2 = this.valueFont.getWidth(strMethod002, padding * s);
            if (strMethod003.isEmpty()) {
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.valueFont, strMethod002, f4 - width2, fY2, padding * s, 0.05f, iHp);
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.heartTexture, ((f4 - width2) - halfDivisor * s) - padding * s, fY2 + (this.valueFont.getHeight(padding * s) / halfDivisor), (int)(iconSize * s), (int)(iconSize * s), iHp);
            } else {
                float width3 = this.valueFont.getWidth(strMethod003, padding * s);
                float f6 = f4 - ((width3 + halfDivisor * s) + padding * s);
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.valueFont, strMethod003, f4 - width3, fY2, padding * s, 0.05f, class115VarColorStack.computeColor(ThemePalette.darkGolden.argb()));
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.gappleTexture, ((f4 - width3) - halfDivisor * s) - padding * s, fY2 + (this.valueFont.getHeight(padding * s) / halfDivisor), (int)(iconSize * s), (int)(iconSize * s), class115VarColorStack.white());
                float f7 = f6 - sectionGap * s;
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.valueFont, strMethod002, f7 - width2, fY2, padding * s, 0.05f, iHp);
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.heartTexture, ((f7 - width2) - halfDivisor * s) - padding * s, fY2 + (this.valueFont.getHeight(padding * s) / halfDivisor), (int)(iconSize * s), (int)(iconSize * s), iHp);
            }
        } else {
            float fBottom3 = this.nameBounds.bottom() - this.valueFont.getHeight(padding * s);
            float f4 = fX2 + fWidth2;
            float width2 = this.valueFont.getWidth(strMethod002, padding * s);
            float f5 = width2 + halfDivisor * s + padding * s;
            if (strMethod003.isEmpty()) {
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.valueFont, strMethod002, f4 - width2, fBottom3, padding * s, 0.05f, iHp);
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.heartTexture, ((f4 - width2) - halfDivisor * s) - padding * s, fBottom3 + (this.valueFont.getHeight(padding * s) / halfDivisor), (int)(iconSize * s), (int)(iconSize * s), iHp);
            } else {
                float width3 = this.valueFont.getWidth(strMethod003, padding * s);
                float f6 = f4 - ((width3 + halfDivisor * s) + padding * s);
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.valueFont, strMethod003, f4 - width3, fBottom3, padding * s, 0.05f, class115VarColorStack.computeColor(ThemePalette.darkGolden.argb()));
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.gappleTexture, ((f4 - width3) - halfDivisor * s) - padding * s, fBottom3 + (this.valueFont.getHeight(padding * s) / halfDivisor), (int)(iconSize * s), (int)(iconSize * s), class115VarColorStack.white());
                float f7 = f6 - sectionGap * s;
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.valueFont, strMethod002, f7 - width2, fBottom3, padding * s, 0.05f, iHp);
                class154VarDrawEngine.textureVerticalC(matrixStack.peek().getPositionMatrix(), this.heartTexture, ((f7 - width2) - halfDivisor * s) - padding * s, fBottom3 + (this.valueFont.getHeight(padding * s) / halfDivisor), (int)(iconSize * s), (int)(iconSize * s), iHp);
            }
        }
        class115VarColorStack.pop();
    }

    public void renderZenith(DrawEngine class154VarDrawEngine, MatrixStack matrixStack, ColorStack class115VarColorStack, ThemePalette class764VarPalette, float fAnimatedValue) {
        float s = this.scale();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        int iBg = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
        int iBg2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());
        int iOutline = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb());
        class154VarDrawEngine.roundedRectangle(matrix4f, this.x, this.y, width(), height(), cornerRadius * s, 2.5f * s, iOutline, iBg2, iBg2, iBg, iBg);
        float fBand = this.avatarBounds.width() + 18.0f * s;
        class154VarDrawEngine.roundedRectangle(matrix4f, this.x, this.y, fBand, height(), cornerRadius * s, 2.5f * s, iOutline, iBg2, iBg2, iBg, iBg);
        renderAvatar(class154VarDrawEngine, matrix4f, class115VarColorStack, this.avatarBounds.x(), this.avatarBounds.y(), this.avatarBounds.width(), this.avatarBounds.height(), 3.0f * s);
        String strMethod008 = getDisplayName(this.target);
        int iName = class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb());
        if (this.nameFont.getWidth(strMethod008, 12.0f * s) > this.nameBounds.width()) {
            class154VarDrawEngine.msdfFontWithHorizontalGradient(matrix4f, this.nameFont, truncateToWidth(this.nameFont, strMethod008, 12, this.nameBounds.width() / s), this.nameBounds.x(), this.nameBounds.y(), 12.0f * s, 0.05f, iName, iName & 16777215);
        } else {
            class154VarDrawEngine.msdfFont(matrix4f, this.nameFont, strMethod008, this.nameBounds.x(), this.nameBounds.y(), 12.0f * s, 0.05f, iName);
        }
        List<ItemStack> listMethod009 = getZenithEquipment(this.target);
        float fX = this.itemsBounds.x();
        for (ItemStack itemStack : listMethod009) {
            renderWidgetItem(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, itemStack, fX, this.itemsBounds.y(), 12.0f * s, 7.0f * s);
            fX += 13.0f * s;
        }
        float realHealth = ScoreboardHelper.INSTANCE.getRealHealth(this.target);
        float fMethod007 = getEffectiveMaxHealth(this.target);
        float f3 = fMethod007 > 0.0f ? realHealth / fMethod007 : 0.0f;
        this.absorptionAnimation.destination(MathUtil.clamp(this.target.getAbsorptionAmount() / 20.0f, 0.0f, 1.0f));
        this.healthAnimation.destination(MathUtil.clamp(f3, 0.0f, 1.0f));
        int iHp = healthColor(class115VarColorStack, this.healthAnimation.animatedValue());
        float fRing = 30.0f * s;
        float fCx = (this.x + width()) - 10.0f * s - (fRing / halfDivisor);
        float fCy = this.y + (height() / halfDivisor);
        drawHpRing(class154VarDrawEngine, matrix4f, class115VarColorStack, class764VarPalette, fCx, fCy, 15.0f * s, 12.5f * s, this.healthAnimation.animatedValue(), shouldShowAbsorption(this.target) ? this.absorptionAnimation.animatedValue() : 0.0f, iHp);
        String strHp = formatHp(this.target, this.healthAnimation.animatedValue() * getEffectiveMaxHealth(this.target));
        class154VarDrawEngine.msdfFont(matrix4f, this.valueFont, strHp, fCx - (this.valueFont.getWidth(strHp, 11.0f * s) / halfDivisor), fCy - (this.valueFont.getHeight(11.0f * s) / halfDivisor), 11.0f * s, 0.05f, iHp);
        class115VarColorStack.pop();
    }

    public void renderNurs(DrawEngine class154VarDrawEngine, MatrixStack matrixStack, ColorStack class115VarColorStack, ThemePalette class764VarPalette, float fAnimatedValue) {
        float s = this.scale();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        int iBg = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
        int iBg2 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());
        int iOutline = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb());
        class154VarDrawEngine.roundedRectangle(matrix4f, this.x, this.y, width(), height(), cornerRadius * s, 2.5f * s, iOutline, iBg2, iBg2, iBg, iBg);
        class154VarDrawEngine.rectangle(matrix4f, this.x + 46.0f * s, this.y + 8.0f * s, 1.0f * s, height() - 16.0f * s, iOutline);
        renderAvatar(class154VarDrawEngine, matrix4f, class115VarColorStack, this.avatarBounds.x(), this.avatarBounds.y(), this.avatarBounds.width(), this.avatarBounds.height(), 8.0f * s);
        float realHealth = ScoreboardHelper.INSTANCE.getRealHealth(this.target);
        float fMethod007 = getEffectiveMaxHealth(this.target);
        float f3 = fMethod007 > 0.0f ? realHealth / fMethod007 : 0.0f;
        this.absorptionAnimation.destination(MathUtil.clamp(this.target.getAbsorptionAmount() / 20.0f, 0.0f, 1.0f));
        this.healthAnimation.destination(MathUtil.clamp(f3, 0.0f, 1.0f));
        int iHp = healthColor(class115VarColorStack, this.healthAnimation.animatedValue());
        float fCx = this.avatarBounds.x() + (this.avatarBounds.width() / halfDivisor);
        float fCy = this.avatarBounds.y() + (this.avatarBounds.height() / halfDivisor);
        drawHpRing(class154VarDrawEngine, matrix4f, class115VarColorStack, class764VarPalette, fCx, fCy, 19.0f * s, 17.0f * s, this.healthAnimation.animatedValue(), shouldShowAbsorption(this.target) ? this.absorptionAnimation.animatedValue() : 0.0f, iHp);
        String strMethod008 = getDisplayName(this.target);
        int iName = class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb());
        if (this.nameFont.getWidth(strMethod008, 11.0f * s) > this.nameBounds.width()) {
            class154VarDrawEngine.msdfFontWithHorizontalGradient(matrix4f, this.nameFont, truncateToWidth(this.nameFont, strMethod008, 11, this.nameBounds.width() / s), this.nameBounds.x(), this.nameBounds.y(), 11.0f * s, 0.05f, iName, iName & 16777215);
        } else {
            class154VarDrawEngine.msdfFont(matrix4f, this.nameFont, strMethod008, this.nameBounds.x(), this.nameBounds.y(), 11.0f * s, 0.05f, iName);
        }
        String strHp = formatHp(this.target, this.healthAnimation.animatedValue() * getEffectiveMaxHealth(this.target));
        class154VarDrawEngine.msdfFont(matrix4f, this.valueFont, strHp, this.contentBounds.right() - this.valueFont.getWidth(strHp, 11.0f * s), this.nameBounds.y() - 1.0f * s, 11.0f * s, 0.05f, iHp);
        float fBarY = this.nameBounds.bottom() + 3.0f * s;
        float fBarW = this.contentBounds.width();
        float fHealth = this.healthAnimation.animatedValue();
        boolean zMethod012 = shouldShowAbsorption(this.target);
        class154VarDrawEngine.roundedRectangle(matrix4f, this.contentBounds.x(), fBarY, fBarW, 2.5f * s, 1.25f * s, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()));
        if (fHealth > 0.0f) {
            class154VarDrawEngine.roundedRectangle(matrix4f, this.contentBounds.x(), fBarY, fBarW * fHealth, 2.5f * s, 1.25f * s, iHp);
        }
        if (zMethod012) {
            float fAbsorb = this.absorptionAnimation.animatedValue();
            class154VarDrawEngine.roundedRectangle(matrix4f, this.contentBounds.x(), fBarY + 5.0f * s, fBarW, 2.5f * s, 1.25f * s, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(500).argb()));
            if (fAbsorb > 0.0f) {
                class154VarDrawEngine.roundedRectangle(matrix4f, this.contentBounds.x(), fBarY + 5.0f * s, fBarW * fAbsorb, 2.5f * s, 1.25f * s, class115VarColorStack.computeColor(ThemePalette.darkGolden.argb()));
            }
        }
        List<ItemStack> listMethod011 = getEquipment(this.target);
        if (!listMethod011.isEmpty()) {
            float fX2 = this.itemsBounds.x();
            for (ItemStack itemStack : listMethod011) {
                renderWidgetItem(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, itemStack, fX2, this.itemsBounds.y(), 12.0f * s, 6.5f * s);
                fX2 += 15.0f * s;
            }
        }
        class115VarColorStack.pop();
    }

    public void renderCelestial(DrawEngine class154VarDrawEngine, MatrixStack matrixStack, ColorStack class115VarColorStack, ThemePalette class764VarPalette, float fAnimatedValue) {
        float s = this.scale();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        Matrix4f matrix4f = matrixStack.peek().getPositionMatrix();
        float realHealth = ScoreboardHelper.INSTANCE.getRealHealth(this.target);
        float fMethod007 = getEffectiveMaxHealth(this.target);
        float fRatio = fMethod007 > 0.0f ? realHealth / fMethod007 : 0.0f;
        this.absorptionAnimation.destination(MathUtil.clamp(this.target.getAbsorptionAmount() / 20.0f, 0.0f, 1.0f));
        this.healthAnimation.destination(MathUtil.clamp(fRatio, 0.0f, 1.0f));

        int iAccent = class115VarColorStack.computeColor(class764VarPalette.accent().argb());
        int iAccentBright = class115VarColorStack.computeColor(class764VarPalette.accentBright().argb());
        int iPanel = class115VarColorStack.computeColor(0xE1000000);
        int iGlow = class115VarColorStack.computeColor(class764VarPalette.accent().argb(), 0.45f);
        int iName = class115VarColorStack.computeColor(0xFFEBEBEB);
        int iTrack = class115VarColorStack.computeColor(0x232323);

        float fRadius = 7.0f * s;
        class154VarDrawEngine.roundedRectangle(matrix4f, this.x - 1.5f * s, this.y - 1.5f * s, width() + 3.0f * s, height() + 3.0f * s, fRadius + 1.5f * s, iGlow);
        class154VarDrawEngine.roundedRectangle(matrix4f, this.x, this.y, width(), height(), fRadius, 1.0f * s, iAccent, iPanel, iPanel, iPanel, iPanel);

        renderAvatar(class154VarDrawEngine, matrix4f, class115VarColorStack, this.avatarBounds.x(), this.avatarBounds.y(), this.avatarBounds.width(), this.avatarBounds.height(), 2.5f * s);

        String strName = getDisplayName(this.target);
        if (this.nameFont.getWidth(strName, 12.0f * s) > this.nameBounds.width()) {
            class154VarDrawEngine.msdfFontWithHorizontalGradient(matrix4f, this.nameFont, truncateToWidth(this.nameFont, strName, 12, this.nameBounds.width() / s), this.nameBounds.x(), this.nameBounds.y(), 12.0f * s, 0.05f, iName, iName & 16777215);
        } else {
            class154VarDrawEngine.msdfFont(matrix4f, this.nameFont, strName, this.nameBounds.x(), this.nameBounds.y(), 12.0f * s, 0.05f, iName);
        }

        float fHpX = this.x + 52.0f * s;
        float fHpY = this.y + 20.0f * s;
        String strHp = "HP: " + formatHp(this.target, this.healthAnimation.animatedValue() * getEffectiveMaxHealth(this.target));
        class154VarDrawEngine.msdfFont(matrix4f, this.valueFont, strHp, fHpX, fHpY, 10.0f * s, 0.05f, iName);
        if (shouldShowAbsorption(this.target)) {
            float fAbsorb = this.absorptionAnimation.animatedValue() * (this.target.getMaxAbsorption() != 0.0f ? this.target.getMaxAbsorption() : 20.0f);
            float fOffset = fHpX + this.valueFont.getWidth(strHp, 10.0f * s);
            class154VarDrawEngine.msdfFont(matrix4f, this.valueFont, " + ", fOffset, fHpY, 10.0f * s, 0.05f, class115VarColorStack.computeColor(ThemePalette.grey.argb()));
            class154VarDrawEngine.msdfFont(matrix4f, this.valueFont, formatValue(fAbsorb) + " AB", fOffset + this.valueFont.getWidth(" + ", 10.0f * s), fHpY, 10.0f * s, 0.05f, class115VarColorStack.computeColor(ThemePalette.golden.argb()));
        }

        float fBarX = this.x + 52.0f * s;
        float fBarY = this.y + 32.0f * s;
        float fBarW = this.width - 60.0f * s;
        float fBarH = 5.0f * s;
        float fBarRadius = 2.5f * s;
        class154VarDrawEngine.roundedRectangle(matrix4f, fBarX, fBarY, fBarW, fBarH, fBarRadius, iTrack);
        float fFill = fBarW * this.healthAnimation.animatedValue();
        if (fFill > 0.0f) {
            class154VarDrawEngine.roundedRectangle(matrix4f, fBarX, fBarY, fFill, fBarH, fBarRadius, iAccent, iAccentBright, iAccent, iAccentBright);
        }
        if (shouldShowAbsorption(this.target) && this.absorptionAnimation.animatedValue() > 0.0f) {
            class154VarDrawEngine.roundedRectangle(matrix4f, fBarX, fBarY, fBarW * this.absorptionAnimation.animatedValue(), fBarH, fBarRadius, class115VarColorStack.computeColor(ThemePalette.darkGolden.argb()));
        }

        List<ItemStack> equipment = getEquipment(this.target);
        float itemW = 13.0f * s;
        float itemGap = 2.0f * s;
        float fX = this.itemsBounds.x();
        for (ItemStack itemStack : equipment) {
            float fPad = 1.5f * s;
            class154VarDrawEngine.roundedRectangle(matrix4f, fX - fPad, this.itemsBounds.y() - fPad, itemW + fPad * 2.0f, itemW + fPad * 2.0f, 3.0f * s, 0.6f * s, class115VarColorStack.computeColor(0xFFFFFF, 0.30f), class115VarColorStack.computeColor(0xFFFFFF, 0.10f));
            renderWidgetItem(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, itemStack, fX, this.itemsBounds.y(), itemW, itemLabelFontSize(s), true);
            fX += itemW + itemCountWidth(itemStack, s) + itemGap;
        }
        class115VarColorStack.pop();
    }

    public void drawHpRing(DrawEngine class154VarDrawEngine, Matrix4f matrix4f, ColorStack class115VarColorStack, ThemePalette class764VarPalette, float f, float f2, float f3, float f4, float f5, float f6, int iHp) {
        int iOutline = class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb());
        class154VarDrawEngine.circle(matrix4f, f, f2, f3, f4, 0.0f, 360.0f, iOutline);
        drawRingArc(class154VarDrawEngine, matrix4f, f, f2, f3, f4, f5, iHp);
        if (f6 > 0.0f) {
            drawRingArc(class154VarDrawEngine, matrix4f, f, f2, f3, f4, f6, class115VarColorStack.computeColor(ThemePalette.darkGolden.argb()));
        }
    }

    public void drawRingArc(DrawEngine class154VarDrawEngine, Matrix4f matrix4f, float f, float f2, float f3, float f4, float fFrac, int iColor) {
        float fClamp = MathUtil.clamp(fFrac, 0.0f, 1.0f);
        if (fClamp <= 0.0f) {
            return;
        }
        if (fClamp >= 1.0f) {
            class154VarDrawEngine.circle(matrix4f, f, f2, f3, f4, 0.0f, 360.0f, iColor);
            return;
        }
        if (fClamp <= 0.25f) {
            class154VarDrawEngine.circle(matrix4f, f, f2, f3, f4, 90.0f - (360.0f * fClamp), 90.0f, iColor);
            return;
        }
        class154VarDrawEngine.circle(matrix4f, f, f2, f3, f4, 450.0f - (360.0f * fClamp), 90.0f, iColor);
    }

    public int healthColor(ColorStack class115VarColorStack, float fFrac) {
        float fClamp = MathUtil.clamp(fFrac, 0.0f, 1.0f);
        if (fClamp >= 0.5f) {
            return class115VarColorStack.interpolate(ThemePalette.deepGreen.argb(), ThemePalette.golden.argb(), (1.0f - fClamp) * 2.0f);
        }
        return class115VarColorStack.interpolate(ThemePalette.golden.argb(), ThemePalette.red.argb(), (0.5f - fClamp) * 2.0f);
    }

    public void renderWidgetItem(DrawEngine class154VarDrawEngine, MatrixStack matrixStack, ColorStack class115VarColorStack, ThemePalette class764VarPalette, ItemStack itemStack, float f, float f2, float f3, float f4) {
        renderWidgetItem(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, itemStack, f, f2, f3, f4, false);
    }

    public void renderWidgetItem(DrawEngine class154VarDrawEngine, MatrixStack matrixStack, ColorStack class115VarColorStack, ThemePalette class764VarPalette, ItemStack itemStack, float f, float f2, float f3, float f4, boolean zLabelsAround) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        class154VarDrawEngine.itemStack(matrixStack.peek().getPositionMatrix(), itemStack, f, f2, f3 / 32.0f, 1.0f);
        if (itemStack.isDamageable() && itemStack.getMaxDamage() > 0) {
            float fDura = ((float) (itemStack.getMaxDamage() - itemStack.getDamage())) / ((float) itemStack.getMaxDamage());
            int iDuraColor;
            if (fDura >= 0.5f) {
                iDuraColor = 0xFF5AE05A;
            } else if (fDura >= 0.25f) {
                iDuraColor = 0xFFFFC040;
            } else {
                iDuraColor = 0xFFFF5050;
            }
            if (zLabelsAround && itemStack.getItem() instanceof ArmorItem) {
                String strDura = Math.round(fDura * 100.0f) + "%";
                MsdfFont duraFont = Fonts.INTER_SEMIBOLD.get();
                float fDuraSize = f4;
                float fDuraW = duraFont.getWidth(strDura, fDuraSize);
                if (fDuraW > f3 - 1.0f) {
                    fDuraSize *= (f3 - 1.0f) / fDuraW;
                    fDuraW = f3 - 1.0f;
                }
                float fDuraX = f + ((f3 - fDuraW) / halfDivisor);
                float fDuraY = (f2 + f3) + (f3 * 0.05f);
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), duraFont, strDura, fDuraX + 0.5f, fDuraY + 0.5f, fDuraSize, 0.0f, class115VarColorStack.computeColor(0x000000, 0.85f));
                class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), duraFont, strDura, fDuraX, fDuraY, fDuraSize, 0.0f, class115VarColorStack.computeColor(iDuraColor, 1.0f));
            } else {
                float fBarH = Math.max(1.0f, f3 * 0.14f);
                float fBarX = f;
                float fBarY = (f2 + f3) - fBarH;
                class154VarDrawEngine.rectangle(matrixStack.peek().getPositionMatrix(), fBarX, fBarY, f3, fBarH, class115VarColorStack.computeColor(0x000000, 0.6f));
                if (fDura > 0.0f) {
                    class154VarDrawEngine.rectangle(matrixStack.peek().getPositionMatrix(), fBarX, fBarY, f3 * fDura, fBarH, class115VarColorStack.computeColor(iDuraColor, 1.0f));
                }
            }
        }
        if (itemStack.getCount() > 1) {
            String strCount = "x" + itemStack.getCount();
            MsdfFont countFont = Fonts.INTER_SEMIBOLD.get();
            float fCountH = countFont.getHeight(f4);
            float fCountW = countFont.getWidth(strCount, f4);
            float fCountX;
            if (zLabelsAround) {
                fCountX = (f + f3) + (f3 * 0.06f);
            } else {
                fCountX = (f + f3) - fCountW - 0.5f;
            }
            float fCountY = (f2 + f3) - fCountH;
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), countFont, strCount, fCountX + 0.5f, fCountY + 0.5f, f4, 0.0f, class115VarColorStack.computeColor(0x000000, 0.85f));
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), countFont, strCount, fCountX, fCountY, f4, 0.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(50).argb()));
        }
    }

    public static float itemLabelFontSize(float s) {
        return 6.5f * s;
    }

    static float itemCountWidth(ItemStack itemStack, float s) {
        if (itemStack == null || itemStack.getCount() <= 1) {
            return 0.0f;
        }
        return Fonts.INTER_SEMIBOLD.get().getWidth("x" + itemStack.getCount(), itemLabelFontSize(s));
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
        this.healthAnimation.animate(class141Var);
        this.absorptionAnimation.animate(class141Var);
        this.fadeAnimation.animate(class141Var);
        this.toggleAnimatorA.animate(class141Var);
        this.toggleAnimatorB.animate(class141Var);
    }

    public void updateTarget() {
        LivingEntity livingEntityMethod001 = findTarget();
        if (livingEntityMethod001 == null) {
            if (this.target != null) {
                this.fadeAnimation.destination(0.0f);
            }
            if (this.fadeAnimation.isZero()) {
                this.target = null;
                this.healthAnimation.set(0.0f);
                this.absorptionAnimation.set(0.0f);
                return;
            }
            return;
        }
        if (!Objects.equals(this.target, livingEntityMethod001)) {
            this.target = livingEntityMethod001;
            float realHealth = ScoreboardHelper.INSTANCE.getRealHealth(livingEntityMethod001);
            float fMethod007 = getEffectiveMaxHealth(livingEntityMethod001);
            this.healthAnimation.set(MathUtil.clamp(fMethod007 > 0.0f ? realHealth / fMethod007 : 0.0f, 0.0f, 1.0f));
            this.absorptionAnimation.set(MathUtil.clamp(livingEntityMethod001.getAbsorptionAmount() / 20.0f, 0.0f, 1.0f));
        }
        this.fadeAnimation.destination(1.0f);
    }

    public void renderAvatar(DrawEngine class154Var, Matrix4f matrix4f, ColorStack class115Var, float f, float f2, float f3, float f4, float f5) {
        Mc class815Var = Mc.INSTANCE;
        if (class815Var.getEntityRenderDispatcher().getRenderer(this.target) instanceof LivingEntityRenderer renderer ) {
            LivingEntityRenderer livingEntityRenderer = renderer;
            float tickDelta = class815Var.getRenderTickCounter().getTickDelta(false);
            AbstractTexture texture = (AbstractTexture) (class815Var.getTextureManager().getTexture(livingEntityRenderer.getTexture((net.minecraft.client.render.entity.state.LivingEntityRenderState) livingEntityRenderer.getAndUpdateRenderState(this.target, tickDelta))));
            ItemSpriteManager class219Var = ItemSpriteManager.INSTANCE;
            if (texture != null) {
                GlTexture orCreateTexture = class219Var.getOrCreateTexture(texture);
                UvBounds faceUV = class219Var.getFaceUV(livingEntityRenderer);
                int i = this.target.hurtTime;
                float f6 = 1.0f - ((i - (i != 0 ? tickDelta : 0.0f)) / padding);
                int iComputeColor = class115Var.computeColor(this.fadeAnimation.animatedValue(), StencilBufferUtil.STENCIL_MASK, (int) (100.0f + (155.0f * f6)), (int) (100.0f + (155.0f * f6)));
                class154Var.roundedTexture(matrix4f, f, f2, f3, f4, f5, f5, f5, f5, faceUV != null ? faceUV.u0() : 0.125f, faceUV != null ? faceUV.v0() : 0.125f, faceUV != null ? faceUV.u1() : 0.25f, faceUV != null ? faceUV.v1() : 0.25f, class154Var.bindTexture(orCreateTexture.textureWithSTB()), iComputeColor, iComputeColor, iComputeColor, iComputeColor);
            }
            int iComputeColor2 = class115Var.computeColor(395026, 0.0f);
            int iComputeColor3 = class115Var.computeColor(395026, 0.5f);
            class154Var.roundedRectangle(matrix4f, f, f2, f3, f4, cornerRadius, iComputeColor3, iComputeColor3, iComputeColor2, iComputeColor2);
        }
    }

    public LivingEntity findTarget() {
        LivingEntity livingEntityMethod005;
        Mc class815Var = Mc.INSTANCE;
        AttackAuraModule class878Var = (AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class);
        if (class878Var.isState() && class878Var.target() != null && !class878Var.getName().isEmpty()) {
            return class878Var.target();
        }
        if (this.showInRaycast.isValue() && (livingEntityMethod005 = raycastTarget(class815Var)) != null && !livingEntityMethod005.getName().getString().isEmpty()) {
            return livingEntityMethod005;
        }
        if (!(class815Var.getCurrentScreen() instanceof ChatScreen) || getDisplayName(class815Var.getPlayer()).isEmpty()) {
            return null;
        }
        return class815Var.getPlayer();
    }

    @Override
    public void update() {
        if (isVisible()) {
            updateTarget();
        }
    }

    @Override
    public float width() {
        return this.width;
    }

    @Override
    public float height() {
        return this.height;
    }

    public float scale() {
        return this.size.currentValue() / 100.0f;
    }

    public static float getEffectiveMaxHealth(LivingEntity livingEntity) {
        float maxHealth = livingEntity.getMaxHealth();
        float realHealth = ScoreboardHelper.INSTANCE.getRealHealth(livingEntity);
        return Math.max(maxHealth, realHealth);
    }

    public static boolean shouldShowAbsorption(LivingEntity livingEntity) {
        return livingEntity != null && !ServerUtil.isConnectedToServer("funtime") && livingEntity.getAbsorptionAmount() > 0.0f && livingEntity.getMaxAbsorption() <= 20.0f;
    }

    public static String formatValue(float f) {
        return String.valueOf(MathUtil.round(f, roundIncrement));
    }

    public static String formatHp(LivingEntity livingEntity, float fHp) {
        // Если сервер упёрся в потолок скорборда (1024), а ванильное HP ниже - реальное значение больше
        if (fHp >= 1024.0f && livingEntity.getHealth() < fHp) {
            return Math.round(fHp) + "+";
        }
        return formatValue(fHp);
    }

    public static List<ItemStack> getEquipment(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return Collections.emptyList();
        }
        List<ItemStack> list = IteratorUtil.toList(livingEntity.getAllArmorItems().iterator());
        Collections.reverse(list);
        list.removeIf((v0) -> {
            return v0.isEmpty();
        });
        ItemStack mainHandStack = livingEntity.getMainHandStack();
        ItemStack offHandStack = livingEntity.getOffHandStack();
        if (!mainHandStack.isEmpty()) {
            list.add(mainHandStack);
        }
        if (!offHandStack.isEmpty()) {
            list.add(offHandStack);
        }
        return list;
    }

    public static List<ItemStack> getZenithEquipment(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return Collections.emptyList();
        }
        List<ItemStack> list = new ArrayList<>();
        list.add(livingEntity.getMainHandStack());
        list.add(livingEntity.getOffHandStack());
        list.add(livingEntity.getEquippedStack(EquipmentSlot.HEAD));
        list.add(livingEntity.getEquippedStack(EquipmentSlot.CHEST));
        list.add(livingEntity.getEquippedStack(EquipmentSlot.LEGS));
        list.add(livingEntity.getEquippedStack(EquipmentSlot.FEET));
        list.removeIf(ItemStack::isEmpty);
        return list;
    }

    public static String truncateToWidth(MsdfFont class161Var, String str, int i, float f) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        if (class161Var.getWidth(str, i) <= f) {
            return str;
        }
        int i2 = 0;
        int length = str.length();
        int i3 = 0;
        while (i2 <= length) {
            int i4 = (i2 + length) >>> 1;
            if (class161Var.getWidth(str.substring(0, i4), i) <= f) {
                i3 = i4;
                i2 = i4 + 1;
            } else {
                length = i4 - 1;
            }
        }
        return i3 <= 0 ? "" : str.substring(0, i3);
    }

    public String getDisplayName(LivingEntity livingEntity) {
        if (livingEntity == null) {
            return "";
        }
        String strStripTextFormat = StringHelper.stripTextFormat(livingEntity.getName().getString());
        NameProtectModule class512Var = (NameProtectModule) Expensive.INSTANCE.moduleRepository().get(NameProtectModule.class);
        return class512Var.isState() ? class512Var.replace(strStripTextFormat) : strStripTextFormat;
    }

    public LivingEntity raycastTarget(Mc class815Var) {
        ClientPlayerEntity player = class815Var.getPlayer();
        Entity cameraEntity = class815Var.getCameraEntity();
        if (player == null || cameraEntity == null || class815Var.getWorld() == null) {
            return null;
        }
        double entityInteractionRange = player.getEntityInteractionRange();
        Vec3d cameraPosVec = cameraEntity.getCameraPosVec(1.0f);
        Vec3d rotationVec = cameraEntity.getRotationVec(1.0f);
        Vec3d vec3dAdd = cameraPosVec.add(rotationVec.multiply(entityInteractionRange));
        List<LivingEntity> entitiesByClass = class815Var.getWorld().getEntitiesByClass(LivingEntity.class, cameraEntity.getBoundingBox().stretch(rotationVec.multiply(entityInteractionRange)).expand(1.0d), livingEntity -> {
            return (livingEntity == player || !livingEntity.isAlive() || livingEntity.isSpectator()) ? false : true;
        });
        LivingEntity livingEntityMethod010 = raycastSingle(this.target, raycastExpand, cameraPosVec, vec3dAdd);
        if (livingEntityMethod010 != null) {
            return livingEntityMethod010;
        }
        if (entitiesByClass.size() <= 2) {
            return findClosestRaycast(entitiesByClass, roundIncrement, cameraPosVec, vec3dAdd);
        }
        return null;
    }

    public LivingEntity findClosestRaycast(List<LivingEntity> list, double d, Vec3d vec3d, Vec3d vec3d2) {
        LivingEntity livingEntity = null;
        double d2 = Double.MAX_VALUE;
        for (LivingEntity livingEntity2 : list) {
            Optional optionalRaycast = livingEntity2.getBoundingBox().expand(d).raycast(vec3d, vec3d2);
            if (!optionalRaycast.isEmpty()) {
                double dSquaredDistanceTo = vec3d.squaredDistanceTo((Vec3d) optionalRaycast.get());
                if (dSquaredDistanceTo < d2) {
                    d2 = dSquaredDistanceTo;
                    livingEntity = livingEntity2;
                }
            }
        }
        return livingEntity;
    }

    public LivingEntity raycastSingle(LivingEntity livingEntity, double d, Vec3d vec3d, Vec3d vec3d2) {
        if (livingEntity != null && livingEntity.isAlive() && livingEntity.getBoundingBox().expand(d).raycast(vec3d, vec3d2).isPresent()) {
            return livingEntity;
        }
        return null;
    }
}
