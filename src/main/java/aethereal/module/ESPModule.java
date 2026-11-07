package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.resource.ClasspathResource;
import aethereal.ui.setting.ColorSetting;
import aethereal.render.ColorStack;
import aethereal.util.DrawEngine;
import aethereal.type.EntityCategory;
import aethereal.model.EntityFilter;
import aethereal.event.EntityRenderEvent;
import aethereal.type.EspBoxStyle;
import aethereal.type.EspDisplayPart;
import aethereal.type.EspEnchant;
import aethereal.model.EspEntityProjection;
import aethereal.model.EspItemProjection;
import aethereal.type.EspTargetType;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.render.Fonts;
import aethereal.util.FriendManager;
import aethereal.render.GlTexture;
import aethereal.type.ItemSpriteManager;
import aethereal.util.IteratorUtil;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.ProjectionUtil;
import aethereal.event.Render2DEvent;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.util.ScoreboardHelper;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;
import aethereal.util.StencilBufferUtil;
import aethereal.util.StringUtil;
import aethereal.render.ThemePalette;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.consume.UseAction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.text.TextVisitFactory;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.joml.Matrix4f;
import org.joml.Vector4f;

@Aliases(aliases = {"ESP", "NameTags", "OffHand ESP", "Tags"})
public class ESPModule extends Module {
    public final MultiSelectSetting<EspTargetType> selectsTargets;
    public final MultiSelectSetting<EspDisplayPart> displayParts;
    public final MultiSelectSetting<EspEnchant> enchantsToShow;
    public final ModeSetting<EspBoxStyle> boxStyle;
    public final NumberSetting scaleSetting;
    public final BooleanSetting ignoreNaked;
    public final ColorSetting entityColor;
    public final ColorSetting itemColor;
    public final GlTexture playerIcon;
    public final GlTexture prefixIcon;
    public final GlTexture friendIcon;
    public final List<EspEntityProjection> entityProjections;
    public final List<EspItemProjection> itemProjections;
    static final int iconSize = 11;
    static final int backpackNormalId = 3;
    static final int spacing = 6;
    static final String[] POTION_ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
    final Map<Integer, Map<RegistryEntry<StatusEffect>, TrackedEffect>> trackedEffects = new HashMap();
    final Map<Integer, Map<RegistryEntry<StatusEffect>, Long>> particleEffects = new HashMap();
    final Map<Integer, DrinkingRecord> drinkingPotions = new HashMap();
    Map<Integer, RegistryEntry<StatusEffect>> effectColorCache;

    public ESPModule() {
        super(ModuleTab.RENDER, "ESP");
        this.selectsTargets = new MultiSelectSetting(Lang.SELECTTARGETS, Lang.SELECTTARGETS_DESC).values(EspTargetType.class);
        this.displayParts = new MultiSelectSetting(Lang.ESP_DISPLAY, Lang.ESP_DISPLAY_DESC).values(EspDisplayPart.class);
        this.enchantsToShow = new MultiSelectSetting(Lang.ESP_ENCHANTS_TO_SHOW, Lang.ESP_ENCHANTS_TO_SHOW_DESC).values(EspEnchant.class).visible(() -> {
            return Boolean.valueOf(this.displayParts.isSelected(EspDisplayPart.ENCHANTS));
        }).select(EspEnchant.values());
        this.boxStyle = new ModeSetting(Lang.ESP_BOX_MODE).values(EspBoxStyle.class).visible(() -> {
            return Boolean.valueOf(this.displayParts.isSelected(EspDisplayPart.BOX));
        });
        this.scaleSetting = new NumberSetting(Lang.ARROWS_SCALE).range(0.5f, 2.0f).currentValue(1.0f).step(0.05f).unit(SettingUnit.PIXELS);
        this.ignoreNaked = new BooleanSetting(Lang.ATTACKAURA_IGNORE_NAKED);
        this.entityColor = new ColorSetting(Lang.SKELETON_COLOR);
        this.itemColor = new ColorSetting(Lang.ESP_ITEMS_COLOR);
        this.playerIcon = new GlTexture(new ClasspathResource("/icons/menu/new/tabs/player.png"));
        this.prefixIcon = new GlTexture(new ClasspathResource("/icons/menu/new/planet.png"));
        this.friendIcon = new GlTexture(new ClasspathResource("/icons/menu/new/star_fill.png"));
        this.entityProjections = new ArrayList();
        this.itemProjections = new ArrayList();
        addSettings(this.selectsTargets, this.displayParts, this.boxStyle, this.enchantsToShow, this.scaleSetting, this.entityColor, this.itemColor, this.ignoreNaked);
        register(EntityRenderEvent.class, class147Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && this.displayParts.isSelected(EspDisplayPart.TAGS) && buildFilter().matches(class147Var.getEntity())) {
                if (this.ignoreNaked.isValue()) {
                    Entity entity = class147Var.getEntity();
                    if ((entity instanceof LivingEntity) && isNaked((LivingEntity) entity)) {
                        return;
                    }
                }
                class147Var.cancel();
            }
        });
        register(PacketReceiveEvent.class, this::onPacketReceive);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && class130Var.isPre() && Mc.INSTANCE.isWorldLoaded()) {
                trackDrinkingPotions();
            }
        });
        register(Render2DEvent.class, class311Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded() && class311Var.isPre()) {
                this.entityProjections.clear();
                this.itemProjections.clear();
                boolean zIsSelected = this.displayParts.isSelected(EspDisplayPart.BOX);
                boolean zIsSelected2 = this.displayParts.isSelected(EspDisplayPart.TAGS);
                boolean zIsSelected3 = this.displayParts.isSelected(EspDisplayPart.HEALTH);
                boolean zIsSelected4 = this.displayParts.isSelected(EspDisplayPart.OFFHAND);
                boolean zIsSelected5 = this.displayParts.isSelected(EspDisplayPart.SHULKER);
                boolean zIsSelected6 = this.displayParts.isSelected(EspDisplayPart.POTIONS);
                if (zIsSelected || zIsSelected2 || zIsSelected3 || zIsSelected4 || zIsSelected5 || zIsSelected6) {
                    Mc class815Var = Mc.INSTANCE;
                    GameOptions gameOptions = class815Var.getGameOptions();
                    ClientWorld world = class815Var.getWorld();
                    LivingEntity player = class815Var.getPlayer();
                    Perspective perspective = gameOptions.getPerspective();
                    MatrixStack matrixStack = class311Var.matrixStack();
                    Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                    DrawEngine class154VarDrawEngine = Expensive.INSTANCE.drawEngine();
                    ColorStack class115VarColorStack = class154VarDrawEngine.colorStack();
                    ThemePalette class764VarPalette = Expensive.INSTANCE.theme().palette();
                    EntityFilter class095VarMethod002 = buildFilter();
                    for (Entity livingEntity : world.getEntities()) {
                        if (livingEntity instanceof ItemEntity) {
                            ItemEntity itemEntity = (ItemEntity) livingEntity;
                            if (this.selectsTargets.isSelected(EspTargetType.ITEMS)) {
                                Optional<Vector4f> optionalBoxToScreen = ProjectionUtil.boxToScreen(itemEntity.getBoundingBox().offset(itemEntity.getPos().multiply(-1.0d)).offset(itemEntity.getLerpedPos(class815Var.getTickDelta())));
                                if (!optionalBoxToScreen.isEmpty()) {
                                    this.itemProjections.add(new EspItemProjection(itemEntity, optionalBoxToScreen.get()));
                                }
                            }
                        } else if (livingEntity instanceof LivingEntity) {
                            LivingEntity livingEntity2 = (LivingEntity) livingEntity;
                            if (livingEntity2.isAlive() && (livingEntity2 != player || perspective != Perspective.FIRST_PERSON)) {
                                if (class095VarMethod002.matches(livingEntity2) && (!this.ignoreNaked.isValue() || !isNaked(livingEntity2))) {
                                    Optional<Vector4f> optionalBoxToScreen2 = ProjectionUtil.boxToScreen(livingEntity2.getBoundingBox().offset(livingEntity2.getPos().multiply(-1.0d)).offset(livingEntity2.getLerpedPos(class815Var.getTickDelta())));
                                    if (!optionalBoxToScreen2.isEmpty()) {
                                        this.entityProjections.add(new EspEntityProjection(livingEntity2, optionalBoxToScreen2.get(), FriendManager.isFriend(livingEntity2.getName().getString())));
                                    }
                                }
                            }
                        }
                    }
                    if (this.entityProjections.isEmpty() && this.itemProjections.isEmpty()) {
                        return;
                    }
                    class154VarDrawEngine.begin();
                    if (!this.itemProjections.isEmpty()) {
                        this.itemProjections.sort(Comparator.comparingDouble(class551Var -> {
                            return -player.squaredDistanceTo(class551Var.item);
                        }));
                        for (EspItemProjection class551Var2 : this.itemProjections) {
                            ItemEntity itemEntity2 = class551Var2.item;
                            Vector4f vector4f = class551Var2.proj;
                            float fX = vector4f.x();
                            float fY = vector4f.y();
                            float fZ = vector4f.z();
                            float fW = vector4f.w();
                            float alpha = this.itemColor.getAlpha();
                            int iComputeColor = class115VarColorStack.computeColor((int) (180.0f * alpha), 0, 0, 0);
                            int iComputeColor2 = class115VarColorStack.computeColor(this.itemColor.getColor(), alpha);
                            if (zIsSelected) {
                                drawBox(class154VarDrawEngine, positionMatrix, fX, fY, fZ, fW, iComputeColor, iComputeColor2);
                            }
                            class115VarColorStack.push();
                            class115VarColorStack.alpha(alpha);
                            if (zIsSelected2) {
                                drawItemLabel(class154VarDrawEngine, matrixStack, positionMatrix, class115VarColorStack, class764VarPalette, itemEntity2, vector4f, fY, zIsSelected5);
                            }
                            class115VarColorStack.pop();
                        }
                    }
                    if (!this.entityProjections.isEmpty()) {
                        this.entityProjections.sort(Comparator.comparingDouble(class549Var -> {
                            return -player.squaredDistanceTo(class549Var.living);
                        }));
                        for (EspEntityProjection class549Var2 : this.entityProjections) {
                            LivingEntity livingEntity3 = class549Var2.living;
                            Vector4f vector4f2 = class549Var2.proj;
                            float fX2 = vector4f2.x();
                            float fY2 = vector4f2.y();
                            float fZ2 = vector4f2.z();
                            float fW2 = vector4f2.w();
                            int iComputeColor3 = class115VarColorStack.computeColor(180, 0, 0, 0);
                            int iComputeColor4 = class549Var2.friend ? class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, 129, StencilBufferUtil.STENCIL_MASK, 135) : class115VarColorStack.computeColor(this.entityColor.getColor(), 1.0f);
                            if (zIsSelected) {
                                drawBox(class154VarDrawEngine, positionMatrix, fX2, fY2, fZ2, fW2, iComputeColor3, iComputeColor4);
                            }
                            if (zIsSelected3) {
                                drawHealthBar(class154VarDrawEngine, positionMatrix, livingEntity3, fX2, fY2, fZ2, fW2, iComputeColor3, iComputeColor4);
                            }
                            if (zIsSelected4) {
                                drawOffhandLabel(class154VarDrawEngine, matrixStack, class115VarColorStack, livingEntity3, class764VarPalette, fX2, fY2, fZ2, fW2);
                            }
                            if (zIsSelected2) {
                                drawNameTag(class154VarDrawEngine, matrixStack, positionMatrix, class115VarColorStack, class764VarPalette, livingEntity3, vector4f2, fY2);
                            }
                            if (zIsSelected6) {
                                drawPotionEffects(class154VarDrawEngine, matrixStack, class115VarColorStack, class764VarPalette, livingEntity3, fX2, fY2, fZ2, fW2, zIsSelected4);
                            }
                        }
                    }
                    class154VarDrawEngine.end();
                }
            }
        }, EventPriority.LOWEST);
    }

    public void drawBox(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        switch (((EspBoxStyle) this.boxStyle.currentValue()).ordinal()) {
            case 0:
                drawOutlineBox(class154Var, matrix4f, f, f2, f3, f4, i, i2);
                break;
            case 1:
                drawCornerBox(class154Var, matrix4f, f, f2, f3, f4, i, i2);
                break;
            case 2:
                drawRoundedBox(class154Var, matrix4f, f, f2, f3, f4, i, i2);
                break;
            case 3:
                drawCelestialBox(class154Var, matrix4f, f, f2, f3, f4, i, i2);
                break;
        }
    }

    public void drawCelestialBox(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        if (f3 < 6.0f || f4 < 6.0f) {
            drawOutlineBox(class154Var, matrix4f, f, f2, f3, f4, i, i2);
            return;
        }
        ColorStack colorStack = class154Var.colorStack();
        float progress = (float) ((System.currentTimeMillis() % 2400L) / 2400.0);
        int colorTop = colorStack.getHSBColor(progress, 0.65f, 1.0f, 1.0f);
        int colorRight = colorStack.getHSBColor(progress + 0.25f, 0.65f, 1.0f, 1.0f);
        int colorBottom = colorStack.getHSBColor(progress + 0.5f, 0.65f, 1.0f, 1.0f);
        int colorLeft = colorStack.getHSBColor(progress + 0.75f, 0.65f, 1.0f, 1.0f);
        float thickness = MathUtil.clamp(Math.min(f3, f4) * 0.03f, 1.0f, 2.5f);
        float glow = thickness * 1.5f;
        class154Var.rectangle(matrix4f, f - glow, f2 - glow, f3 + glow * 2.0f, thickness + glow, colorStack.computeColor(colorTop, 0.25f));
        class154Var.rectangle(matrix4f, f - glow, (f2 + f4) - thickness, f3 + glow * 2.0f, thickness + glow, colorStack.computeColor(colorBottom, 0.25f));
        class154Var.rectangle(matrix4f, f - glow, f2, thickness + glow, f4, colorStack.computeColor(colorLeft, 0.25f));
        class154Var.rectangle(matrix4f, (f + f3) - thickness, f2, thickness + glow, f4, colorStack.computeColor(colorRight, 0.25f));
        class154Var.rectangle(matrix4f, f + thickness, f2, f3 - thickness * 2.0f, thickness, colorLeft, colorTop, colorTop, colorLeft);
        class154Var.rectangle(matrix4f, f + thickness, (f2 + f4) - thickness, f3 - thickness * 2.0f, thickness, colorLeft, colorBottom, colorBottom, colorLeft);
        class154Var.rectangle(matrix4f, f, f2 + thickness, thickness, f4 - thickness * 2.0f, colorTop, colorTop, colorBottom, colorBottom);
        class154Var.rectangle(matrix4f, (f + f3) - thickness, f2 + thickness, thickness, f4 - thickness * 2.0f, colorTop, colorTop, colorBottom, colorBottom);
    }

    public void drawOutlineBox(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        drawEdges(class154Var, matrix4f, f - 1.0f, f2 - 1.0f, f3 + 2.0f, f4 + 2.0f, 1.0f, i);
        drawEdges(class154Var, matrix4f, f, f2, f3, f4, 2.0f, i2);
        float f5 = f + 2.0f;
        float f6 = f2 + 2.0f;
        float f7 = f3 - 4.0f;
        float f8 = f4 - 4.0f;
        if (f7 <= 0.0f || f8 <= 0.0f) {
            return;
        }
        drawEdges(class154Var, matrix4f, f5, f6, f7, f8, 1.0f, i);
    }

    public boolean isNaked(LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity) {
            return StreamSupport.stream(((PlayerEntity) livingEntity).getArmorItems().spliterator(), false).allMatch((v0) -> {
                return v0.isEmpty();
            });
        }
        return false;
    }

    public void drawRoundedBox(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        class154Var.roundedRectangle(matrix4f, f - 1.0f, f2 - 1.0f, f3 + 2.0f, f4 + 2.0f, 4.0f + 1.0f, 2.5f, i, 0);
        class154Var.roundedRectangle(matrix4f, f + 2.0f, f2 + 2.0f, f3 - (2.0f * 2.0f), f4 - (2.0f * 2.0f), Math.max(0.0f, 4.0f - 2.0f), 2.5f, i, 0);
        class154Var.roundedRectangle(matrix4f, f, f2, f3, f4, 4.0f, 4.0f, i2, 0);
    }

    public void drawCornerBox(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, int i, int i2) {
        float fMin = Math.min(f3, f4);
        if (fMin < 8.0f) {
            drawOutlineBox(class154Var, matrix4f, f, f2, f3, f4, i, i2);
            return;
        }
        float fClamp = MathUtil.clamp(fMin * 0.25f, 3.0f, Math.min(18.0f, fMin * 0.45f));
        drawCorners(class154Var, matrix4f, f - 1.0f, f2 - 1.0f, f3 + 2.0f, f4 + 2.0f, fClamp + 1.0f, 1.0f, i);
        drawCorners(class154Var, matrix4f, f, f2, f3, f4, fClamp, 2.0f, i2);
        float f5 = f + 2.0f;
        float f6 = f2 + 2.0f;
        float f7 = f3 - 4.0f;
        float f8 = f4 - 4.0f;
        if (f7 <= 0.0f || f8 <= 0.0f) {
            return;
        }
        drawCorners(class154Var, matrix4f, f5, f6, f7, f8, Math.max(0.0f, fClamp - 2.0f), 1.0f, i);
    }

    public void drawCorners(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, float f6, int i) {
        if (f5 <= 0.0f) {
            return;
        }
        class154Var.rectangle(matrix4f, f, f2, f5, f6, i);
        class154Var.rectangle(matrix4f, f, f2, f6, f5, i);
        class154Var.rectangle(matrix4f, (f + f3) - f5, f2, f5, f6, i);
        class154Var.rectangle(matrix4f, (f + f3) - f6, f2, f6, f5, i);
        class154Var.rectangle(matrix4f, f, (f2 + f4) - f6, f5, f6, i);
        class154Var.rectangle(matrix4f, f, (f2 + f4) - f5, f6, f5, i);
        class154Var.rectangle(matrix4f, (f + f3) - f5, (f2 + f4) - f6, f5, f6, i);
        class154Var.rectangle(matrix4f, (f + f3) - f6, (f2 + f4) - f5, f6, f5, i);
    }

    public void drawEdges(DrawEngine class154Var, Matrix4f matrix4f, float f, float f2, float f3, float f4, float f5, int i) {
        class154Var.rectangle(matrix4f, f, f2, f5, f4, i);
        class154Var.rectangle(matrix4f, f + f5, f2, f3 - (2.0f * f5), f5, i);
        class154Var.rectangle(matrix4f, (f + f3) - f5, f2, f5, f4, i);
        class154Var.rectangle(matrix4f, f + f5, (f2 + f4) - f5, f3 - (2.0f * f5), f5, i);
    }

    public void drawNameTag(DrawEngine class154Var, MatrixStack matrixStack, Matrix4f matrix4f, ColorStack class115Var, ThemePalette class764Var, LivingEntity livingEntity, Vector4f vector4f, float f) {
        float f2;
        float fClamp = MathUtil.clamp(this.scaleSetting.currentValue(), 0.5f, 2.0f);
        DoubleUnaryOperator doubleUnaryOperator = d -> {
            return d * ((double) fClamp);
        };
        List<ItemStack> list = IteratorUtil.toList(livingEntity.getAllArmorItems().iterator());
        Collections.reverse(list);
        list.removeIf(itemStack -> {
            return itemStack.isEmpty() || !ItemSpriteManager.INSTANCE.canRender(itemStack);
        });
        ItemStack stackInHand = livingEntity.getStackInHand(Hand.MAIN_HAND);
        ItemStack stackInHand2 = livingEntity.getStackInHand(Hand.OFF_HAND);
        boolean z = !stackInHand.isEmpty() && ItemSpriteManager.INSTANCE.canRender(stackInHand);
        boolean z2 = !stackInHand2.isEmpty() && ItemSpriteManager.INSTANCE.canRender(stackInHand2);
        int healthBelowName = (int) ScoreboardHelper.INSTANCE.getHealthBelowName(livingEntity);
        Team scoreboardTeam = livingEntity.getScoreboardTeam();
        String strTrim = StringUtil.removeSymbols(TextVisitFactory.removeFormattingCodes(scoreboardTeam == null ? Text.empty() : scoreboardTeam.getPrefix())).trim();
        boolean z3 = !strTrim.isEmpty();
        String strStripTextFormat = StringHelper.stripTextFormat(livingEntity.getName().getString());
        NameProtectModule class512Var = (NameProtectModule) Expensive.INSTANCE.moduleRepository().get(NameProtectModule.class);
        if (class512Var.isState()) {
            strStripTextFormat = class512Var.replace(strStripTextFormat);
        }
        String str = healthBelowName + " HP";
        float fApplyAsDouble = (float) doubleUnaryOperator.applyAsDouble(8.0d);
        float fApplyAsDouble2 = (float) doubleUnaryOperator.applyAsDouble(12.0d);
        float fApplyAsDouble3 = (float) doubleUnaryOperator.applyAsDouble(10.0d);
        int iApplyAsDouble = (int) doubleUnaryOperator.applyAsDouble(11.0d);
        float fApplyAsDouble4 = (float) doubleUnaryOperator.applyAsDouble(3.0d);
        float fApplyAsDouble5 = (float) doubleUnaryOperator.applyAsDouble(6.0d);
        float fApplyAsDouble6 = (float) doubleUnaryOperator.applyAsDouble(12.0d);
        float fApplyAsDouble7 = (float) doubleUnaryOperator.applyAsDouble(10.0d);
        float fApplyAsDouble8 = (float) doubleUnaryOperator.applyAsDouble(10.0d);
        float fEnchantGap = (float) doubleUnaryOperator.applyAsDouble(3.0d);
        boolean zEnchants = this.displayParts.isSelected(EspDisplayPart.ENCHANTS);
        float width = z3 ? Fonts.INTER_BOLD.get().getWidth(strTrim, fApplyAsDouble7) : 0.0f;
        float width2 = Fonts.INTER_BOLD.get().getWidth(strStripTextFormat, fApplyAsDouble6);
        float width3 = Fonts.INTER_EXTRA_BOLD.get().getWidth(str, fApplyAsDouble8);
        int i = (z ? 1 : 0) + (z2 ? 1 : 0);
        float f3 = i > 0 ? (i * iApplyAsDouble) + ((i - 1) * fApplyAsDouble4) : 0.0f;
        int size = list.size();
        float f4 = iApplyAsDouble + fApplyAsDouble5 + width2 + fApplyAsDouble2 + (z3 ? iApplyAsDouble + fApplyAsDouble5 + width + fApplyAsDouble2 : 0.0f) + width3 + (size > 0 ? fApplyAsDouble2 + (size > 0 ? (size * iApplyAsDouble) + ((size - 1) * fApplyAsDouble4) : 0.0f) : 0.0f);
        if (i > 0) {
            f2 = (size > 0 ? fApplyAsDouble3 : fApplyAsDouble2) + f3;
        } else {
            f2 = 0.0f;
        }
        float f5 = (fApplyAsDouble * 2.0f) + f4 + f2;
        float height = Fonts.INTER_BOLD.get().getHeight(fApplyAsDouble6) + ((float) doubleUnaryOperator.applyAsDouble(6.0d));
        float fCenterX = ProjectionUtil.centerX(vector4f) - (f5 / 2.0f);
        float fApplyAsDouble9 = f - ((float) doubleUnaryOperator.applyAsDouble(30.0d));
        float f6 = fApplyAsDouble9 + (height / 2.0f);
        class115Var.push();
        class115Var.alpha(this.entityColor.getAlpha());
        class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), fCenterX, fApplyAsDouble9, f5, height, (float) doubleUnaryOperator.applyAsDouble(7.0d), (float) doubleUnaryOperator.applyAsDouble(2.5d), class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
        class115Var.pop();
        float f7 = fCenterX + fApplyAsDouble;
        class154Var.textureVerticalC(matrix4f, this.playerIcon, f7, f6, iApplyAsDouble, iApplyAsDouble, class115Var.computeColor(class764Var.accent().argb()));
        float f8 = f7 + iApplyAsDouble + fApplyAsDouble5;
        float height2 = f6 - (Fonts.INTER_BOLD.get().getHeight(fApplyAsDouble6) / 2.0f);
        int iComputeColor = class115Var.computeColor(class764Var.text().tone(400).argb());
        class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), strStripTextFormat, f8, height2, fApplyAsDouble6, 0.05f, iComputeColor);
        float f9 = f8 + width2 + fApplyAsDouble2;
        if (z3) {
            class154Var.textureVerticalC(matrix4f, this.prefixIcon, f9, f6, iApplyAsDouble, iApplyAsDouble, class115Var.computeColor(class764Var.accent().argb()));
            float f10 = f9 + iApplyAsDouble + fApplyAsDouble5;
            class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), strTrim, f10, f6 - (Fonts.INTER_BOLD.get().getHeight(fApplyAsDouble7) / 2.0f), fApplyAsDouble7, 0.05f, iComputeColor);
            f9 = f10 + width + fApplyAsDouble2;
        }
        class154Var.msdfFont(matrix4f, Fonts.INTER_EXTRA_BOLD.get(), str, f9, f6 - (Fonts.INTER_EXTRA_BOLD.get().getHeight(fApplyAsDouble8) / 2.0f), fApplyAsDouble8, 0.05f, iComputeColor);
        float f11 = f9 + width3 + fApplyAsDouble2;
        Iterator it = list.iterator();
        while (it.hasNext()) {
            ItemStack stackItem = (ItemStack) it.next();
            class154Var.itemStack(matrixStack.peek().getPositionMatrix(), stackItem, f11, f6 - (iApplyAsDouble / 2.0f), (float) doubleUnaryOperator.applyAsDouble(0.34375d), 1.0f);
            if (stackItem.isDamageable() && stackItem.getMaxDamage() > 0) {
                class115Var.push();
                class115Var.alpha(this.entityColor.getAlpha());
                float fRatio = ((float) (stackItem.getMaxDamage() - stackItem.getDamage())) / ((float) stackItem.getMaxDamage());
                float fBarW = (float) doubleUnaryOperator.applyAsDouble(9.0d);
                float fBarH = (float) doubleUnaryOperator.applyAsDouble(1.5d);
                float fBarX = f11 + ((float) iApplyAsDouble / 2.0f) - (fBarW / 2.0f);
                float fBarY = f6 + ((float) iApplyAsDouble / 2.0f) + (float) doubleUnaryOperator.applyAsDouble(1.0d);
                int iBarBg = class115Var.computeColor(class764Var.surfaceBackground().tone(300).argb(), 0.9f);
                class154Var.rectangle(matrixStack.peek().getPositionMatrix(), fBarX, fBarY, fBarW, fBarH, iBarBg);
                if (fRatio > 0.0f) {
                    int iBarColor = class115Var.computeColor(getDurabilityBarColor(fRatio), 1.0f);
                    class154Var.rectangle(matrixStack.peek().getPositionMatrix(), fBarX, fBarY, fBarW * fRatio, fBarH, iBarColor);
                }
                class115Var.pop();
            }
            if (zEnchants) {
                drawEnchantLines(class154Var, matrix4f, class115Var, class764Var, stackItem, f11 + (iApplyAsDouble / 2.0f), f6 - (iApplyAsDouble / 2.0f), fApplyAsDouble, fEnchantGap);
            }
            f11 += iApplyAsDouble + fApplyAsDouble4;
        }
        if (i > 0) {
            if (size > 0) {
                f11 += fApplyAsDouble3;
            }
            if (z) {
                class154Var.itemStack(matrixStack.peek().getPositionMatrix(), stackInHand, f11, f6 - (iApplyAsDouble / 2.0f), (float) doubleUnaryOperator.applyAsDouble(0.34375d), 1.0f);
                if (zEnchants) {
                    drawEnchantLines(class154Var, matrix4f, class115Var, class764Var, stackInHand, f11 + (iApplyAsDouble / 2.0f), f6 - (iApplyAsDouble / 2.0f), fApplyAsDouble, fEnchantGap);
                }
                f11 += iApplyAsDouble + fApplyAsDouble4;
            }
            if (z2) {
                class154Var.itemStack(matrixStack.peek().getPositionMatrix(), stackInHand2, f11, f6 - (iApplyAsDouble / 2.0f), (float) doubleUnaryOperator.applyAsDouble(0.34375d), 1.0f);
                if (zEnchants) {
                    drawEnchantLines(class154Var, matrix4f, class115Var, class764Var, stackInHand2, f11 + (iApplyAsDouble / 2.0f), f6 - (iApplyAsDouble / 2.0f), fApplyAsDouble, fEnchantGap);
                }
            }
        }
        if (FriendManager.isFriend(livingEntity.getName().getString())) {
            int iApplyAsDouble2 = (int) doubleUnaryOperator.applyAsDouble(8.0d);
            int iApplyAsDouble3 = (int) doubleUnaryOperator.applyAsDouble(10.0d);
            float fApplyAsDouble10 = (float) doubleUnaryOperator.applyAsDouble(5.0d);
            float fApplyAsDouble11 = (float) doubleUnaryOperator.applyAsDouble(4.0d);
            float width4 = (fApplyAsDouble10 * 2.0f) + iApplyAsDouble3 + fApplyAsDouble11 + Fonts.INTER_EXTRA_BOLD.get().getWidth("FRIEND", iApplyAsDouble2);
            float fApplyAsDouble12 = (float) doubleUnaryOperator.applyAsDouble(14.0d);
            float fApplyAsDouble13 = width4 + ((float) doubleUnaryOperator.applyAsDouble(8.0d));
            float fApplyAsDouble14 = (float) doubleUnaryOperator.applyAsDouble(22.0d);
            float f12 = (fCenterX + (f5 / 2.0f)) - (fApplyAsDouble13 / 2.0f);
            float fApplyAsDouble15 = (float) (((double) (fApplyAsDouble9 - fApplyAsDouble14)) + doubleUnaryOperator.applyAsDouble(2.0d));
            class115Var.push();
            class115Var.alpha(this.entityColor.getAlpha());
            class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), f12, fApplyAsDouble15, fApplyAsDouble13, fApplyAsDouble14, (float) doubleUnaryOperator.applyAsDouble(8.0d), (float) doubleUnaryOperator.applyAsDouble(0.0d), (float) doubleUnaryOperator.applyAsDouble(8.0d), (float) doubleUnaryOperator.applyAsDouble(0.0d), (float) doubleUnaryOperator.applyAsDouble(2.5d), class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
            float f13 = (f12 + (fApplyAsDouble13 / 2.0f)) - (width4 / 2.0f);
            float f14 = (fApplyAsDouble15 + (fApplyAsDouble14 / 2.0f)) - (fApplyAsDouble12 / 2.0f);
            class154Var.roundedRectangle(matrix4f, f13, f14, width4, fApplyAsDouble12, 5.0f, class115Var.computeColor(5023589, 0.1f));
            class115Var.pop();
            class154Var.textureVerticalC(matrix4f, this.friendIcon, f13 + fApplyAsDouble10, f14 + (fApplyAsDouble12 / 2.0f), iApplyAsDouble3, iApplyAsDouble3, class115Var.computeColor(ThemePalette.deepGreen.argb()));
            class154Var.msdfFont(matrix4f, Fonts.INTER_EXTRA_BOLD.get(), "FRIEND", f13 + fApplyAsDouble10 + iApplyAsDouble3 + fApplyAsDouble11, (f14 + (fApplyAsDouble12 / 2.0f)) - (Fonts.INTER_EXTRA_BOLD.get().getHeight(iApplyAsDouble2) / 2.0f), iApplyAsDouble2, 0.05f, class115Var.computeColor(ThemePalette.deepGreen.argb()));
        }
    }

    public void drawHealthBar(DrawEngine class154Var, Matrix4f matrix4f, LivingEntity livingEntity, float f, float f2, float f3, float f4, int i, int i2) {
        float fClamp = MathUtil.clamp(ScoreboardHelper.INSTANCE.getHealthBelowName(livingEntity) / Math.max(0.001f, Math.min(livingEntity.getMaxHealth(), 20.0f)), 0.0f, 1.0f);
        if (fClamp == 0.0f) {
            return;
        }
        float f5 = f + f3 + 5.0f;
        class154Var.rectangle(matrix4f, f5, f2, 4.0f, f4, i);
        class154Var.rectangle(matrix4f, f5 + 1.0f, f2 + 1.0f + ((f4 - 2.0f) * (1.0f - fClamp)), 2.0f, (f4 - 2.0f) * fClamp, i2);
    }

    public void drawOffhandLabel(DrawEngine class154Var, MatrixStack matrixStack, ColorStack class115Var, LivingEntity livingEntity, ThemePalette class764Var, float f, float f2, float f3, float f4) {
        float fClamp = MathUtil.clamp(this.scaleSetting.currentValue(), 0.5f, 2.0f);
        DoubleUnaryOperator doubleUnaryOperator = d -> {
            return d * ((double) fClamp);
        };
        ItemStack stackInHand = livingEntity.getStackInHand(Hand.OFF_HAND);
        if (stackInHand.isEmpty()) {
            return;
        }
        String strTrim = StringHelper.stripTextFormat(stackInHand.getName().getString()).replace("[â˜…]", "").replace("fff", "").replace("ggg", "").replace("xxx", "").trim();
        float fApplyAsDouble = (float) doubleUnaryOperator.applyAsDouble(12.0d);
        float width = Fonts.INTER_BOLD.get().getWidth(strTrim, fApplyAsDouble);
        float fApplyAsDouble2 = (float) doubleUnaryOperator.applyAsDouble(8.0d);
        float fApplyAsDouble3 = (float) doubleUnaryOperator.applyAsDouble(8.0d);
        float fApplyAsDouble4 = (float) doubleUnaryOperator.applyAsDouble(11.0d);
        float f5 = (fApplyAsDouble2 * 2.0f) + fApplyAsDouble4 + fApplyAsDouble3 + width;
        float height = Fonts.INTER_BOLD.get().getHeight(fApplyAsDouble) + ((float) doubleUnaryOperator.applyAsDouble(6.0d));
        float f6 = (f + (f3 / 2.0f)) - (f5 / 2.0f);
        float fApplyAsDouble5 = f2 + f4 + ((float) doubleUnaryOperator.applyAsDouble(4.0d));
        class115Var.push();
        class115Var.alpha(this.entityColor.getAlpha());
        class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), f6, fApplyAsDouble5, f5, height, (float) doubleUnaryOperator.applyAsDouble(7.0d), (float) doubleUnaryOperator.applyAsDouble(2.5d), class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
        class115Var.pop();
        int iComputeColor = class115Var.computeColor(class764Var.text().tone(400).argb());
        float f7 = f6 + fApplyAsDouble2;
        class154Var.itemStack(matrixStack.peek().getPositionMatrix(), stackInHand, f7, (fApplyAsDouble5 + (height / 2.0f)) - (fApplyAsDouble4 / 2.0f), (float) doubleUnaryOperator.applyAsDouble(0.34375d), 1.0f);
        class154Var.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_BOLD.get(), strTrim, f7 + fApplyAsDouble4 + fApplyAsDouble3, (fApplyAsDouble5 + (height / 2.0f)) - (Fonts.INTER_BOLD.get().getHeight(fApplyAsDouble) / 2.0f), fApplyAsDouble, 0.0f, iComputeColor);
    }

    public void drawPotionEffects(DrawEngine class154Var, MatrixStack matrixStack, ColorStack class115Var, ThemePalette class764Var, LivingEntity livingEntity, float f, float f2, float f3, float f4, boolean zBelowOffhand) {
        ArrayList<StatusEffectInstance> listEffects = new ArrayList();
        for (StatusEffectInstance effect : collectPotionEffects(livingEntity)) {
            if (effect != null) {
                listEffects.add(effect);
            }
        }
        if (listEffects.isEmpty()) {
            return;
        }
        listEffects.sort(Comparator.comparing(effect -> effect.getEffectType().value().getName().getString()));
        float fClamp = MathUtil.clamp(this.scaleSetting.currentValue(), 0.5f, 2.0f);
        DoubleUnaryOperator doubleUnaryOperator = d -> {
            return d * ((double) fClamp);
        };
        float fTextSize = (float) doubleUnaryOperator.applyAsDouble(9.0d);
        float fGap = (float) doubleUnaryOperator.applyAsDouble(2.0d);
        float fPadding = (float) doubleUnaryOperator.applyAsDouble(3.0d);
        float fTextHeight = Fonts.INTER_BOLD.get().getHeight(fTextSize);
        float fRowHeight = fTextHeight + fGap;
        float fPlateHeight = (fRowHeight * ((float) listEffects.size())) + (fPadding * 2.0f) - fGap;
        float fMaxWidth = 0.0f;
        for (StatusEffectInstance effect : listEffects) {
            float fWidth = Fonts.INTER_BOLD.get().getWidth(potionLine(effect), fTextSize);
            if (fWidth > fMaxWidth) {
                fMaxWidth = fWidth;
            }
        }
        float fPlateWidth = fMaxWidth + (fPadding * 2.0f);
        float fCenterX = (f + (f3 / 2.0f)) - (fPlateWidth / 2.0f);
        boolean zOffhandPlate = zBelowOffhand && !livingEntity.getStackInHand(Hand.OFF_HAND).isEmpty();
        float fTop = zOffhandPlate
                ? (f2 + f4 + ((float) doubleUnaryOperator.applyAsDouble(4.0d)) + Fonts.INTER_BOLD.get().getHeight((float) doubleUnaryOperator.applyAsDouble(12.0d)) + ((float) doubleUnaryOperator.applyAsDouble(6.0d)))
                : (f2 + f4 + ((float) doubleUnaryOperator.applyAsDouble(4.0d)));
        class115Var.push();
        class115Var.alpha(this.entityColor.getAlpha());
        class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), fCenterX, fTop, fPlateWidth, fPlateHeight, (float) doubleUnaryOperator.applyAsDouble(5.0d), (float) doubleUnaryOperator.applyAsDouble(2.0d), class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
        float fTextX = fCenterX + fPadding;
        float fTextY = fTop + fPadding + (fTextHeight / 2.0f);
        for (StatusEffectInstance effect : listEffects) {
            boolean zBeneficial = effect.getEffectType().value().isBeneficial();
            int iColor = class115Var.computeColor(zBeneficial ? 0xFF5AE05A : 0xFFFF5050, 1.0f);
            class154Var.msdfFont(matrixStack.peek().getPositionMatrix(), Fonts.INTER_BOLD.get(), potionLine(effect), fTextX, fTextY - (fTextHeight / 2.0f), fTextSize, 0.05f, iColor);
            fTextY += fRowHeight;
        }
        class115Var.pop();
    }

    public String potionLine(StatusEffectInstance effect) {
        String strName = effect.getEffectType().value().getName().getString();
        String strLevel = "";
        int iAmplifier = effect.getAmplifier();
        if (iAmplifier > 0 && iAmplifier < POTION_ROMAN.length) {
            strLevel = " " + POTION_ROMAN[iAmplifier];
        }
        return strName + strLevel + " " + formatPotionTime(effect.getDuration());
    }

    public String formatPotionTime(int iTicks) {
        if (iTicks < 0) {
            return "\u221E";
        }
        int iSeconds = iTicks / 20;
        if (iSeconds > 3600) {
            return "\u221E";
        }
        return String.format("%d:%02d", iSeconds / 60, iSeconds % 60);
    }

    public void drawItemLabel(DrawEngine class154Var, MatrixStack matrixStack, Matrix4f matrix4f, ColorStack class115Var, ThemePalette class764Var, ItemEntity itemEntity, Vector4f vector4f, float f, boolean z) {
        float fClamp = MathUtil.clamp(this.scaleSetting.currentValue(), 0.5f, 2.0f);
        DoubleUnaryOperator doubleUnaryOperator = d -> {
            return d * ((double) fClamp);
        };
        ItemStack stack = itemEntity.getStack();
        int count = stack.getCount();
        String strName = StringHelper.stripTextFormat(stack.getName().getString()).replace("[â˜…]", "").replace("fff", "").replace("ggg", "").replace("xxx", "").trim();
        String strCount = count > 1 ? " x" + count : "";
        String str = strName + strCount;
        float fApplyAsDouble = (float) doubleUnaryOperator.applyAsDouble(11.0d);
        float width = Fonts.INTER_BOLD.get().getWidth(str, fApplyAsDouble);
        float fApplyAsDouble2 = (float) doubleUnaryOperator.applyAsDouble(6.0d);
        float fApplyAsDouble3 = (float) doubleUnaryOperator.applyAsDouble(11.0d);
        float fApplyAsDouble4 = (float) doubleUnaryOperator.applyAsDouble(5.0d);
        float f2 = (fApplyAsDouble2 * 2.0f) + fApplyAsDouble3 + fApplyAsDouble4 + width;
        float height = Fonts.INTER_BOLD.get().getHeight(fApplyAsDouble);
        float fApplyAsDouble5 = height + ((float) doubleUnaryOperator.applyAsDouble(5.0d));
        float fCenterX = ProjectionUtil.centerX(vector4f) - (f2 / 2.0f);
        float fApplyAsDouble6 = f - ((float) doubleUnaryOperator.applyAsDouble(20.0d));
        float f3 = fApplyAsDouble6 + (fApplyAsDouble5 / 2.0f);
        class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), fCenterX, fApplyAsDouble6, f2, fApplyAsDouble5, (float) doubleUnaryOperator.applyAsDouble(6.0d), (float) doubleUnaryOperator.applyAsDouble(2.0d), class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
        float f4 = fCenterX + fApplyAsDouble2;
        class154Var.itemStack(matrixStack.peek().getPositionMatrix(), stack, f4, f3 - (fApplyAsDouble3 / 2.0f), (float) doubleUnaryOperator.applyAsDouble(0.34375d), 1.0f);
        float fTextX = f4 + fApplyAsDouble3 + fApplyAsDouble4;
        float fTextY = f3 - (height / 2.0f);
        class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), strName, fTextX, fTextY, fApplyAsDouble, 0.0f, class115Var.computeColor(class764Var.text().tone(400).argb()));
        if (count > 1) {
            float fCountX = fTextX + Fonts.INTER_BOLD.get().getWidth(strName, fApplyAsDouble);
            int iCountColor = class115Var.computeColor(0xFFFF5555, 1.0f);
            class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), strCount, fCountX + 0.5f, fTextY + 0.5f, fApplyAsDouble, 0.0f, class115Var.computeColor(0x000000, 0.85f));
            class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), strCount, fCountX, fTextY, fApplyAsDouble, 0.0f, iCountColor);
        }
        if (z) {
            if ((stack.getItem() instanceof BlockItem) && (((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)) {
                List<ItemStack> containerStacks = getContainerStacks(stack);
                if (containerStacks.isEmpty()) {
                    return;
                }
                int iMin = Math.min(containerStacks.size(), 9);
                int iCeil = (int) Math.ceil(((double) containerStacks.size()) / 9.0d);
                float fApplyAsDouble7 = (float) doubleUnaryOperator.applyAsDouble(11.0d);
                float fApplyAsDouble8 = (float) doubleUnaryOperator.applyAsDouble(2.0d);
                float fApplyAsDouble9 = (float) doubleUnaryOperator.applyAsDouble(5.0d);
                float fApplyAsDouble10 = (float) doubleUnaryOperator.applyAsDouble(3.0d);
                float fMax = (iMin * fApplyAsDouble7) + (Math.max(0, iMin - 1) * fApplyAsDouble8);
                float fMax2 = (iCeil * fApplyAsDouble7) + (Math.max(0, iCeil - 1) * fApplyAsDouble8);
                float f5 = (fApplyAsDouble9 * 2.0f) + fMax;
                float f6 = (fApplyAsDouble9 * 2.0f) + fMax2;
                float fCenterX2 = ProjectionUtil.centerX(vector4f) - (f5 / 2.0f);
                float f7 = fApplyAsDouble6 + fApplyAsDouble5 + fApplyAsDouble10;
                class154Var.roundedRectangle(matrixStack.peek().getPositionMatrix(), fCenterX2, f7, f5, f6, (float) doubleUnaryOperator.applyAsDouble(6.0d), (float) doubleUnaryOperator.applyAsDouble(2.0d), class115Var.computeColor(class764Var.surfaceOutline().tone(700).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(801).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()), class115Var.computeColor(class764Var.surfaceBackground().tone(900).argb()));
                float f8 = fCenterX2 + fApplyAsDouble9;
                float f9 = f7 + fApplyAsDouble9;
                float fApplyAsDouble11 = (float) doubleUnaryOperator.applyAsDouble(7.0d);
                int iComputeColor = class115Var.computeColor(class764Var.text().tone(100).argb());
                for (int i = 0; i < containerStacks.size(); i++) {
                    if (i > 0 && i % 9 == 0) {
                        f8 = f8;
                        f9 += fApplyAsDouble7 + fApplyAsDouble8;
                    }
                    ItemStack itemStack = containerStacks.get(i);
                    class154Var.itemStack(matrix4f, itemStack, f8, f9, (float) doubleUnaryOperator.applyAsDouble(0.34375d), 1.0f);
                    if (itemStack.getCount() > 1) {
                        String strValueOf = String.valueOf(itemStack.getCount());
                        float width2 = Fonts.INTER_EXTRA_BOLD.get().getWidth(strValueOf, fApplyAsDouble11);
                        float height2 = Fonts.INTER_EXTRA_BOLD.get().getHeight(fApplyAsDouble11);
                        float f10 = ((f8 + fApplyAsDouble7) - width2) + 2.0f;
                        float f11 = ((f9 + fApplyAsDouble7) - height2) + 2.0f;
                        class154Var.msdfFont(matrix4f, Fonts.INTER_EXTRA_BOLD.get(), strValueOf, f10 + 1.0f, f11 + 1.0f, fApplyAsDouble11, 0.05f, class115Var.computeColor(180, 0, 0, 0));
                        class154Var.msdfFont(matrix4f, Fonts.INTER_EXTRA_BOLD.get(), strValueOf, f10, f11, fApplyAsDouble11, 0.05f, iComputeColor);
                    }
                    f8 += fApplyAsDouble7 + fApplyAsDouble8;
                }
            }
        }
    }

    public record EnchantLine(String text, boolean overLevel) {
    }

    public List<EnchantLine> getEnchantLines(ItemStack itemStack) {
        ArrayList<EnchantLine> list = new ArrayList();
        if (this.enchantsToShow.selectedValues().isEmpty()) {
            return list;
        }
        for (EspEnchant enchant : EspEnchant.values()) {
            if (!this.enchantsToShow.isSelected(enchant)) {
                continue;
            }
            int level = enchant.getLevel(itemStack);
            if (level <= 0) {
                continue;
            }
            String letter = enchant.displayName.effective().substring(0, 1);
            String str = level > 1 ? letter + level : letter;
            list.add(new EnchantLine(str, level > enchant.getMaxLevel()));
        }
        return list;
    }

    public void drawEnchantLines(DrawEngine class154Var, Matrix4f matrix4f, ColorStack class115Var, ThemePalette class764Var, ItemStack itemStack, float fIconCenterX, float fIconTop, float fTextSize, float fGap) {
        List<EnchantLine> list = getEnchantLines(itemStack);
        if (list.isEmpty()) {
            return;
        }
        float fHeight = Fonts.INTER_BOLD.get().getHeight(fTextSize);
        float fStep = fHeight + fGap;
        class115Var.push();
        class115Var.alpha(this.entityColor.getAlpha());
        int iColor = class115Var.computeColor(class764Var.text().tone(200).argb());
        int iOverLevel = class115Var.computeColor(0xFFFF4040, 1.0f);
        int iShadow = class115Var.computeColor(0x000000, 0.85f);
        for (int j = 0; j < list.size(); j++) {
            EnchantLine enchantLine = list.get(j);
            String str = enchantLine.text();
            float fWidth = Fonts.INTER_BOLD.get().getWidth(str, fTextSize);
            float fY = (fIconTop - fGap - fHeight) - ((float) j * fStep);
            float fX = fIconCenterX - (fWidth / 2.0f);
            class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), str, fX + 0.5f, fY + 0.5f, fTextSize, 0.05f, iShadow);
            class154Var.msdfFont(matrix4f, Fonts.INTER_BOLD.get(), str, fX, fY, fTextSize, 0.05f, enchantLine.overLevel() ? iOverLevel : iColor);
        }
        class115Var.pop();
    }

    public void onPacketReceive(PacketReceiveEvent class037Var) {
        Packet<?> class282VarPacket = class037Var.getPacket();
        if (class282VarPacket instanceof EntityStatusEffectS2CPacket packet) {
            RegistryEntry<StatusEffect> registryEntry = packet.getEffectId();
            int iDuration = packet.getDuration();
            long lExpire;
            if (iDuration <= 0) {
                lExpire = Long.MAX_VALUE;
            } else {
                lExpire = System.currentTimeMillis() + (((long) iDuration) * 50L);
            }
            this.trackedEffects.computeIfAbsent(Integer.valueOf(packet.getEntityId()), integer -> {
                return new HashMap();
            }).put(registryEntry, new TrackedEffect(registryEntry, packet.getAmplifier(), packet.isAmbient(), packet.shouldShowParticles(), packet.shouldShowIcon(), (StatusEffectInstance) null, lExpire));
        } else if (class282VarPacket instanceof RemoveEntityStatusEffectS2CPacket class037Var2) {
            ClientWorld world = Mc.INSTANCE.getWorld();
            if (world != null && class037Var2.getEntity(world) instanceof LivingEntity livingEntity) {
                Map map = this.trackedEffects.get(Integer.valueOf(livingEntity.getId()));
                if (map != null) {
                    map.remove(class037Var2.effect());
                    if (map.isEmpty()) {
                        this.trackedEffects.remove(Integer.valueOf(livingEntity.getId()));
                    }
                }
            }
        } else if (class282VarPacket instanceof EntityEquipmentUpdateS2CPacket equipmentPacket) {
            ClientWorld world = Mc.INSTANCE.getWorld();
            if (world != null && world.getEntityById(equipmentPacket.getEntityId()) instanceof PlayerEntity playerEntity) {
                if (playerEntity.getActiveHand() != null && playerEntity.getItemUseTime() >= playerEntity.getStackInHand(playerEntity.getActiveHand()).getMaxUseTime(playerEntity) - 1) {
                    ItemStack stackInHand = playerEntity.getMainHandStack();
                    if (playerEntity.getActiveHand() == Hand.OFF_HAND) {
                        stackInHand = playerEntity.getOffHandStack();
                    }
                    if (stackInHand.isOf(Items.GOLDEN_APPLE)) {
                        applyTrackedEffects(playerEntity.getId(), new StatusEffectInstance[]{
                                new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 0, true, false, true),
                                new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1, true, false, true),
                                new StatusEffectInstance(StatusEffects.RESISTANCE, 100, 0, true, false, true)
                        });
                    } else if (stackInHand.isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
                        applyTrackedEffects(playerEntity.getId(), new StatusEffectInstance[]{
                                new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 3, true, false, true),
                                new StatusEffectInstance(StatusEffects.REGENERATION, 600, 4, true, false, true),
                                new StatusEffectInstance(StatusEffects.RESISTANCE, 6000, 0, true, false, true),
                                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 6000, 0, true, false, true)
                        });
                    }
                }
            }
        } else if (class282VarPacket instanceof ParticleS2CPacket class037Var3) {
            if (class037Var3.getParameters() instanceof EntityEffectParticleEffect particle) {
                ClientWorld world = Mc.INSTANCE.getWorld();
                if (world != null) {
                    RegistryEntry<StatusEffect> registryEntry = matchParticleColor(((int) (particle.getRed() * 255.0F) << 16) | ((int) (particle.getGreen() * 255.0F) << 8) | (int) (particle.getBlue() * 255.0F));
                    if (registryEntry != null) {
                        for (PlayerEntity player : world.getPlayers()) {
                            if (player == Mc.INSTANCE.getPlayer() || !player.isAlive()) {
                                continue;
                            }
                            if (player.getBoundingBox().expand(0.8).contains(class037Var3.getX(), class037Var3.getY(), class037Var3.getZ())) {
                                this.particleEffects.computeIfAbsent(Integer.valueOf(player.getId()), integer -> {
                                    return new HashMap();
                                }).put(registryEntry, System.currentTimeMillis());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void applyTrackedEffects(int iEntityId, StatusEffectInstance[] instances) {
        Map<RegistryEntry<StatusEffect>, TrackedEffect> map = this.trackedEffects.computeIfAbsent(Integer.valueOf(iEntityId), integer -> {
            return new HashMap();
        });
        for (StatusEffectInstance instance : instances) {
            long lExpire = System.currentTimeMillis() + (((long) instance.getDuration()) * 50L);
            map.put(instance.getEffectType(), new TrackedEffect(instance.getEffectType(), instance.getAmplifier(), true, instance.shouldShowParticles(), true, (StatusEffectInstance) null, lExpire));
        }
    }

    public void trackDrinkingPotions() {
        ClientWorld world = Mc.INSTANCE.getWorld();
        ClientPlayerEntity clientPlayer = Mc.INSTANCE.getPlayer();
        if (world == null || clientPlayer == null) {
            return;
        }
        for (PlayerEntity player : world.getPlayers()) {
            if (player == clientPlayer || !player.isAlive()) {
                continue;
            }
            ItemStack active = player.getActiveItem();
            if (active.getItem() instanceof PotionItem) {
                this.drinkingPotions.put(Integer.valueOf(player.getId()), new DrinkingRecord(active.copy(), player.getItemUseTime()));
            }
        }
        Iterator iterator = this.drinkingPotions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            int iId = ((Integer) entry.getKey()).intValue();
            DrinkingRecord record = (DrinkingRecord) entry.getValue();
            Entity entity = world.getEntityById(iId);
            boolean blStillDrinking;
            if (entity instanceof PlayerEntity player) {
                blStillDrinking = player.isAlive() && player.getActiveItem().getItem() instanceof PotionItem;
            } else {
                blStillDrinking = false;
            }
            if (blStillDrinking) {
                continue;
            }
            iterator.remove();
            ItemStack stackInHand = record.stack;
            if (stackInHand.getItem() instanceof PotionItem) {
                PotionContentsComponent contents = stackInHand.get(DataComponentTypes.POTION_CONTENTS);
                if (contents != null) {
                    ArrayList<StatusEffectInstance> listEffects = new ArrayList();
                    for (StatusEffectInstance instance : contents.getEffects()) {
                        listEffects.add(instance);
                    }
                    applyTrackedEffects(iId, listEffects.toArray(new StatusEffectInstance[0]));
                }
            }
        }
    }

    static class DrinkingRecord {
        final ItemStack stack;
        final int useTime;

        DrinkingRecord(ItemStack stack, int useTime) {
            this.stack = stack;
            this.useTime = useTime;
        }
    }

    public RegistryEntry<StatusEffect> matchParticleColor(int iRgb) {
        if (this.effectColorCache == null) {
            Map map = new HashMap();
            for (StatusEffect effect : Registries.STATUS_EFFECT) {
                map.putIfAbsent(Integer.valueOf(effect.getColor()), Registries.STATUS_EFFECT.getEntry(effect));
            }
            this.effectColorCache = map;
        }
        int iR = (iRgb >> 16) & 255;
        int iG = (iRgb >> 8) & 255;
        int iB = iRgb & 255;
        RegistryEntry best = null;
        int iBestDist = 2147483647;
        Iterator iterator = this.effectColorCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            int iColor = ((Integer) entry.getKey()).intValue();
            int iDr = iR - ((iColor >> 16) & 255);
            int iDg = iG - ((iColor >> 8) & 255);
            int iDb = iB - (iColor & 255);
            int iDist = (iDr * iDr) + (iDg * iDg) + (iDb * iDb);
            if (iDist < iBestDist) {
                iBestDist = iDist;
                best = (RegistryEntry) entry.getValue();
            }
        }
        return iBestDist <= 600 ? best : null;
    }

    public Collection<StatusEffectInstance> collectPotionEffects(LivingEntity livingEntity) {
        long lNow = System.currentTimeMillis();
        ArrayList<StatusEffectInstance> result = new ArrayList();
        // Базово берём ванильный список (даже у чужих сущностей клиент обновляет его в реальном времени из пакетов/entity data),
        // дальше дополняем его данными из нашего трекера и частицами как подстраховкой.
        for (StatusEffectInstance instance : livingEntity.getStatusEffects()) {
            if (instance.getDuration() > 0) {
                result.add(instance);
            }
        }
        Map<RegistryEntry<StatusEffect>, TrackedEffect> tracked = this.trackedEffects.get(Integer.valueOf(livingEntity.getId()));
        if (tracked != null) {
            Iterator iterator = tracked.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                TrackedEffect t = (TrackedEffect) entry.getValue();
                int iTicks;
                if (t.expireAt == Long.MAX_VALUE) {
                    iTicks = -1;
                } else {
                    long lRemaining = t.expireAt - lNow;
                    if (lRemaining <= 0L) {
                        iterator.remove();
                        continue;
                    }
                    iTicks = (int) (lRemaining / 50L);
                }
                if (containsEffect(result, t.effect)) {
                    continue;
                }
                result.add(new StatusEffectInstance(t.effect, iTicks, t.amplifier, t.ambient, t.showParticles, t.showIcon, t.hidden));
            }
        }
        Map<RegistryEntry<StatusEffect>, Long> particle = this.particleEffects.get(Integer.valueOf(livingEntity.getId()));
        if (particle != null) {
            Iterator iterator = particle.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                long lSeen = ((Long) entry.getValue()).longValue();
                long lAge = lNow - lSeen;
                if (lAge > 1500L) {
                    iterator.remove();
                    continue;
                }
                if (containsEffect(result, (RegistryEntry) entry.getKey())) {
                    continue;
                }
                int iLife = (int) ((1500L - lAge) / 50L) + 1;
                result.add(new StatusEffectInstance((RegistryEntry) entry.getKey(), iLife, 0, true, false, true, (StatusEffectInstance) null));
            }
        }
        return result;
    }

    public boolean containsEffect(List<StatusEffectInstance> list, RegistryEntry<StatusEffect> registryEntry) {
        for (StatusEffectInstance instance : list) {
            if (instance.getEffectType() == registryEntry) {
                return true;
            }
        }
        return false;
    }

    static class TrackedEffect {
        final RegistryEntry<StatusEffect> effect;
        final int amplifier;
        final boolean ambient;
        final boolean showParticles;
        final boolean showIcon;
        final StatusEffectInstance hidden;
        final long expireAt;

        TrackedEffect(RegistryEntry<StatusEffect> effect, int amplifier, boolean ambient, boolean showParticles, boolean showIcon, StatusEffectInstance hidden, long expireAt) {
            this.effect = effect;
            this.amplifier = amplifier;
            this.ambient = ambient;
            this.showParticles = showParticles;
            this.showIcon = showIcon;
            this.hidden = hidden;
            this.expireAt = expireAt;
        }
    }

    public int getDurabilityBarColor(float fRatio) {
        if (fRatio >= 0.5f) {
            return 0xFF5AE05A;
        }
        if (fRatio >= 0.25f) {
            return 0xFFFFC040;
        }
        return 0xFFFF5050;
    }

    public EntityFilter buildFilter() {
        EntityFilter class095Var = new EntityFilter();
        if (this.selectsTargets.isSelected(EspTargetType.SELF)) {
            class095Var.add(EntityCategory.SELF);
        }
        if (this.selectsTargets.isSelected(EspTargetType.PLAYERS)) {
            class095Var.add(EntityCategory.PLAYER);
        }
        if (this.selectsTargets.isSelected(EspTargetType.FRIENDS)) {
            class095Var.add(EntityCategory.FRIEND);
        }
        if (this.selectsTargets.isSelected(EspTargetType.MOBS)) {
            class095Var.add(EntityCategory.MOB);
        }
        if (this.selectsTargets.isSelected(EspTargetType.ANIMALS)) {
            class095Var.add(EntityCategory.ANIMAL);
        }
        return class095Var;
    }

    public List<ItemStack> getContainerStacks(ItemStack itemStack) {
        ArrayList arrayList = new ArrayList(((ContainerComponent) itemStack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT)).stream().filter(itemStack2 -> {
            return !itemStack2.isEmpty();
        }).toList());
        if (!arrayList.isEmpty()) {
            return arrayList;
        }
        NbtCompound nbtCompoundCopyNbt = ((NbtComponent) itemStack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)).copyNbt();
        NbtList list = nbtCompoundCopyNbt.getList("Items", 10);
        if (list.isEmpty()) {
            list = nbtCompoundCopyNbt.getCompound("BlockEntityTag").getList("Items", 10);
        }
        if (!list.isEmpty()) {
            DynamicRegistryManager registryManager = Mc.INSTANCE.getWorld().getRegistryManager();
            for (int i = 0; i < list.size(); i++) {
                Optional optionalFilter = ItemStack.fromNbt(registryManager, list.getCompound(i)).filter(itemStack3 -> {
                    return !itemStack3.isEmpty();
                });
                Objects.requireNonNull(arrayList);
                optionalFilter.ifPresent((v1) -> {
                    arrayList.add(v1);
                });
            }
            if (!arrayList.isEmpty()) {
                return arrayList;
            }
        }
        if (ServerUtil.isConnectedToServer("holyworld")) {
            Optional.ofNullable(nbtCompoundCopyNbt.getList("backpack-inventory", 10)).ifPresent(nbtList -> {
                HashMap map = new HashMap();
                Iterator it = nbtList.iterator();
                while (it.hasNext()) {
                    if (((NbtElement) it.next()) instanceof NbtCompound nbtCompound2) {
                        NbtCompound compound = nbtCompound2.getCompound("item");
                        map.put(Integer.valueOf(nbtCompound2.getInt("slot")), new ItemStack((ItemConvertible) Registries.ITEM.get(Identifier.of(compound.getString("id"))), compound.getInt("Count")));
                    }
                }
                if (map.isEmpty()) {
                    return;
                }
                IntStream.range(0, Math.max(maxBackPackSlots(nbtCompoundCopyNbt), 27)).forEach(i2 -> {
                    arrayList.add((ItemStack) map.getOrDefault(Integer.valueOf(i2), Items.AIR.getDefaultStack()));
                });
            });
        }
        return arrayList;
    }

    public int maxBackPackSlots(NbtCompound nbtCompound) {
        String strReplace = nbtCompound.getCompound("PublicBukkitValues").getString("litebackpacks:backpack").replace("\"", "");
        byte b = -1;
        switch (strReplace.hashCode()) {
            case -1039745817:
                if (strReplace.equals("normal")) {
                    b = backpackNormalId;
                }
                break;
            case 97536:
                if (strReplace.equals("big")) {
                    b = 2;
                }
                break;
            case 3213995:
                if (strReplace.equals("huge")) {
                    b = 1;
                }
                break;
            case 3351639:
                if (strReplace.equals("mini")) {
                    b = 4;
                }
                break;
            case 173173288:
                if (strReplace.equals("infinity")) {
                    b = 0;
                }
                break;
        }
        switch (b) {
            case 0:
                return 36;
            case 1:
                return 27;
            case 2:
                return 21;
            case backpackNormalId:
                return 15;
            case 4:
                return 9;
            default:
                return -1;
        }
    }

    public MultiSelectSetting<EspTargetType> getSelectsTargets() {
        return this.selectsTargets;
    }
}
