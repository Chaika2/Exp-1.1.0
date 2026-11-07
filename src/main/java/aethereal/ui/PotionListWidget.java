package aethereal.ui;
import aethereal.render.AnimatedFloat;
import aethereal.resource.ClasspathResource;
import aethereal.render.ColorStack;
import aethereal.model.DragRenderContext;
import aethereal.util.DrawEngine;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.render.GlTexture;
import aethereal.util.InventoryUtil;
import aethereal.type.Mc;
import aethereal.model.MouseButtonInput2;
import aethereal.model.MouseMoveInput;
import aethereal.render.MsdfFont;
import aethereal.type.NotificationType;
import aethereal.model.PotionEntry;
import aethereal.model.PotionKey;
import aethereal.render.ThemePalette;
import aethereal.util.WeightedEngine;
import aethereal.model.WidgetBounds;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class PotionListWidget extends Draggable {
    public final MsdfFont nameFont;
    public final MsdfFont durationFont;
    public final GlTexture starsTexture;
    public final GlTexture iconTexture;
    public final Map<PotionKey, PotionEntry> entries;
    public final AnimatedFloat openAnimation;
    public final WidgetBounds headerBounds;
    public float width;
    public float height;
    public int frameCounter;

    public PotionListWidget(BooleanSupplier booleanSupplier) {
        super("PotionList", booleanSupplier);
        this.nameFont = Fonts.INTER_SEMIBOLD.get();
        this.durationFont = Fonts.INTER_EXTRA_BOLD.get();
        this.starsTexture = new GlTexture(new ClasspathResource("/textures/stars.png"));
        this.iconTexture = new GlTexture(new ClasspathResource("/icons/menu/new/test_tube.png"));
        this.entries = new HashMap();
        this.openAnimation = new AnimatedFloat(250, Easings.LINEAR);
        this.headerBounds = new WidgetBounds(0.0f, 0.0f, 0.0f, 19.0f);
        this.width = 150.0f;
        this.height = 39.0f;
        this.x = 183.0f;
        this.y = 49.0f;
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
    public boolean click(MouseButtonInput2 class807Var, boolean z) {
        return false;
    }

    @Override
    public boolean cursor(MouseMoveInput class808Var, boolean z) {
        return z && !class808Var.intercepted();
    }

    public List<PotionEntry> getVisibleEntries() {
        return this.entries.values().stream().filter(class648Var -> {
            return (class648Var.animator.isZero() || class648Var.effect == null) ? false : true;
        }).sorted(Comparator.<PotionEntry, Integer>comparing(class648Var2 -> {
            return Integer.valueOf(class648Var2.harmful ? 0 : 1);
        }).thenComparingDouble(class648Var3 -> {
            return -this.nameFont.getWidth(class648Var3.getDisplayName(), 12.0f);
        })).toList();
    }

    @Override
    public void layout(DragRenderContext class809Var) {
        if (isVisible()) {
            float fMax = 150.0f;
            float fMin = 39.0f;
            float fMax2 = 0.0f;
            List<PotionEntry> listMethod008 = getVisibleEntries();
            int i = -1;
            for (int i2 = 0; i2 < listMethod008.size(); i2++) {
                if (listMethod008.get(i2).animator.smoothAnimation() > 0.001f) {
                    i = i2;
                }
            }
            int i3 = 0;
            while (i3 < listMethod008.size()) {
                PotionEntry class648Var = listMethod008.get(i3);
                float fMin2 = Math.min(class648Var.animator.smoothAnimation(), 1.0f);
                float width = this.nameFont.getWidth(class648Var.getDisplayName(), 12.0f);
                float width2 = 12.0f + (class648Var.infinite ? 0.0f : 10.0f) + (class648Var.infinite ? 0.0f : 5.0f) + this.durationFont.getWidth("00:00", 10.0f);
                float height = 6.0f + this.durationFont.getHeight(10.0f);
                fMax = Math.max(fMax, 150.0f + (((((20.0f + width) + 40.0f) + width2) - 150.0f) * fMin2));
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
        int iComputeColor;
        int iComputeColor2;
        int iComputeColor3;
        if (!isVisible() || this.openAnimation.isZero()) {
            return;
        }
        DrawEngine class154VarDrawEngine = class809Var.drawEngine();
        MatrixStack matrixStack = class809Var.matrixStack();
        ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
        ThemePalette class764VarPalette = class809Var.theme().palette();
        float fAnimatedValue = this.openAnimation.animatedValue();
        class115VarColorStack.push();
        class115VarColorStack.alpha(fAnimatedValue);
        int iComputeColor4 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(801).argb());
        int iComputeColor5 = class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(900).argb());
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, 8.0f, 2.5f, class115VarColorStack.computeColor(class764VarPalette.surfaceOutline().tone(600).argb()), iComputeColor5, iComputeColor5, iComputeColor4, iComputeColor4);
        class154VarDrawEngine.texture(matrixStack.peek().getPositionMatrix(), this.x, this.y, this.width, this.height, class154VarDrawEngine.bindTexture(this.starsTexture.textureWithSTB()), class115VarColorStack.white());
        class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.nameFont, "Potions", this.headerBounds.x(), (this.headerBounds.y() + 9.5f) - (this.nameFont.getHeight(13.0f) / 2.0f), 13.0f, 0.0f, class115VarColorStack.computeColor(class764VarPalette.text().tone(200).argb()));
        class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), this.headerBounds.right() - 19.0f, this.headerBounds.y(), 19.0f, 19.0f, 6.0f, class115VarColorStack.computeColor(class764VarPalette.accentBright().argb(), 0.1f));
        class154VarDrawEngine.textureVerticalCHorizontalC(matrixStack.peek().getPositionMatrix(), this.iconTexture, this.headerBounds.right() - 9.5f, this.headerBounds.y() + 9.5f, 11, 11, class115VarColorStack.computeColor(class764VarPalette.accent().argb()));
        float fBottom = this.headerBounds.bottom() + 12.0f;
        for (PotionEntry class648Var : getVisibleEntries()) {
            float fMin = Math.min(class648Var.animator.smoothAnimation(), 1.0f);
            float width = 12.0f + (class648Var.infinite ? 0.0f : 10.0f) + (class648Var.infinite ? 0.0f : 5.0f) + this.durationFont.getWidth(class648Var.durationText, 10.0f);
            float height = 6.0f + this.durationFont.getHeight(10.0f);
            float f = fAnimatedValue * fMin;
            float fRight = this.headerBounds.right() - width;
            float f2 = this.x + (10.0f * f);
            float fMax = class648Var.maxDuration <= 0 ? 0.0f : Math.max(0.0f, Math.min(1.0f, class648Var.duration / class648Var.maxDuration));
            boolean z = !class648Var.infinite && class648Var.duration > 0 && class648Var.duration < 600;
            if (class648Var.harmful) {
                iComputeColor = ThemePalette.darkRed.argb();
                iComputeColor2 = ThemePalette.darkRed.argb();
                iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
            } else if (z) {
                iComputeColor = ThemePalette.darkOrange.argb();
                iComputeColor2 = ThemePalette.darkOrange.argb();
                iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
            } else {
                iComputeColor = class115VarColorStack.computeColor(class764VarPalette.accent().argb());
                iComputeColor2 = class115VarColorStack.computeColor(class764VarPalette.text().tone(500).argb());
                iComputeColor3 = class115VarColorStack.computeColor(class764VarPalette.text().tone(400).argb());
            }
            class115VarColorStack.push();
            class115VarColorStack.alpha(fMin);
            Sprite sprite = Mc.INSTANCE.getMinecraft().getStatusEffectSpriteManager().getSprite(class648Var.effect);
            class154VarDrawEngine.texture(matrixStack.peek().getPositionMatrix(), f2, (fBottom + (height / 2.0f)) - 7.0f, 14.0f, 14.0f, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(), class154VarDrawEngine.bindTexture(Mc.INSTANCE.getTextureManager().getTexture(sprite.getAtlasId()).getGlId()), class115VarColorStack.white());
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.nameFont, class648Var.getDisplayName(), f2 + 20.0f, (fBottom + (height / 2.0f)) - (this.nameFont.getHeight(12.0f) / 2.0f), 12.0f, 0.05f, class115VarColorStack.computeColor(iComputeColor3));
            class154VarDrawEngine.roundedRectangle(matrixStack.peek().getPositionMatrix(), fRight, fBottom, width, height, 6.0f, class115VarColorStack.computeColor(1974050, 0.5f));
            if (!class648Var.infinite) {
                float f3 = fRight + 11.0f;
                float f4 = fBottom + (height / 2.0f);
                class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f3, f4, 5.0f, 0.0f, 360.0f, 2.0f, class115VarColorStack.computeColor(class764VarPalette.surfaceBackground().tone(400).argb()));
                float f5 = 270.0f - (360.0f * fMax);
                if (f5 >= 0.0f) {
                    class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f3, f4, 5.0f, f5, 270.0f, 2.0f, class115VarColorStack.computeColor(iComputeColor));
                } else {
                    class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f3, f4, 5.0f, 360.0f + f5, 360.0f, 2.0f, class115VarColorStack.computeColor(iComputeColor));
                    class154VarDrawEngine.arc(matrixStack.peek().getPositionMatrix(), f3, f4, 5.0f, 0.0f, 270.0f, 2.0f, class115VarColorStack.computeColor(iComputeColor));
                }
            }
            class154VarDrawEngine.msdfFont(matrixStack.peek().getPositionMatrix(), this.durationFont, class648Var.durationText, fRight + (class648Var.infinite ? 6.0f : 22.0f), (fBottom + (height / 2.0f)) - (this.durationFont.getHeight(10.0f) / 2.0f), 10.0f, 0.05f, class115VarColorStack.computeColor(iComputeColor2));
            class115VarColorStack.pop();
            fBottom += (height + 6.0f) * fMin;
        }
        class115VarColorStack.pop();
    }

    @Override
    public void animate(WeightedEngine class141Var) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return;
        }
        this.frameCounter++;
        for (StatusEffectInstance statusEffectInstance : player.getStatusEffects()) {
            PotionEntry class648VarComputeIfAbsent = this.entries.computeIfAbsent(new PotionKey(Registries.STATUS_EFFECT.getId((StatusEffect) statusEffectInstance.getEffectType().value()), statusEffectInstance.getAmplifier()), class649Var -> {
                return new PotionEntry();
            });
            class648VarComputeIfAbsent.lastUpdateTick = this.frameCounter;
            boolean z = statusEffectInstance.isInfinite() || statusEffectInstance.getDuration() > 0;
            class648VarComputeIfAbsent.animator.state(z).animate(class141Var);
            if (z) {
                class648VarComputeIfAbsent.hasBeenActive = true;
            }
            class648VarComputeIfAbsent.effect = statusEffectInstance.getEffectType();
            class648VarComputeIfAbsent.harmful = ((StatusEffect) statusEffectInstance.getEffectType().value()).getCategory() == StatusEffectCategory.HARMFUL;
            class648VarComputeIfAbsent.amplifier = statusEffectInstance.getAmplifier();
            class648VarComputeIfAbsent.translationKey = statusEffectInstance.getTranslationKey();
            class648VarComputeIfAbsent.infinite = statusEffectInstance.isInfinite();
            class648VarComputeIfAbsent.duration = statusEffectInstance.getDuration();
            class648VarComputeIfAbsent.durationText = InventoryUtil.INSTANCE.getPotionDuration(statusEffectInstance);
            if (!class648VarComputeIfAbsent.infinite && class648VarComputeIfAbsent.duration > class648VarComputeIfAbsent.previousDuration + 5) {
                class648VarComputeIfAbsent.expiryNotified = false;
            }
            if (!class648VarComputeIfAbsent.infinite && isBuffEffect(class648VarComputeIfAbsent.effect) && !class648VarComputeIfAbsent.expiryNotified && class648VarComputeIfAbsent.previousDuration > 20 && class648VarComputeIfAbsent.duration <= 20) {
                Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, buildExpiryMessage(class648VarComputeIfAbsent), 2500L);
                class648VarComputeIfAbsent.expiryNotified = true;
            }
            class648VarComputeIfAbsent.previousDuration = class648VarComputeIfAbsent.duration;
            if (class648VarComputeIfAbsent.infinite) {
                class648VarComputeIfAbsent.maxDuration = -1;
            } else if (class648VarComputeIfAbsent.maxDuration <= 0 || class648VarComputeIfAbsent.duration > class648VarComputeIfAbsent.maxDuration) {
                class648VarComputeIfAbsent.maxDuration = class648VarComputeIfAbsent.duration;
            }
        }
        for (PotionEntry class648Var : this.entries.values()) {
            if (class648Var.lastUpdateTick != this.frameCounter) {
                class648Var.animator.state(false).animate(class141Var);
                class648Var.infinite = false;
            }
        }
        this.entries.entrySet().removeIf(entry -> {
            return this.frameCounter - ((PotionEntry) entry.getValue()).lastUpdateTick > 40 && ((PotionEntry) entry.getValue()).animator.isZero();
        });
        this.openAnimation.destination(!player.getStatusEffects().isEmpty() || (Mc.INSTANCE.getCurrentScreen() instanceof ChatScreen) ? 1.0f : 0.0f).animate(class141Var);
    }

    public boolean isBuffEffect(RegistryEntry<StatusEffect> registryEntry) {
        Identifier id;
        return (registryEntry == null || (id = Registries.STATUS_EFFECT.getId((StatusEffect) registryEntry.value())) == null || (!id.equals(Registries.STATUS_EFFECT.getId((StatusEffect) StatusEffects.STRENGTH.value())) && !id.equals(Registries.STATUS_EFFECT.getId((StatusEffect) StatusEffects.SPEED.value())))) ? false : true;
    }

    public Text buildExpiryMessage(PotionEntry class648Var) {
        return Text.literal("Эффект " + String.valueOf(Formatting.RED) + (I18n.translate(class648Var.translationKey, new Object[0]) + (class648Var.amplifier > 0 ? " " + (class648Var.amplifier + 1) : "")) + String.valueOf(Formatting.RESET) + " закончился!");
    }
}
