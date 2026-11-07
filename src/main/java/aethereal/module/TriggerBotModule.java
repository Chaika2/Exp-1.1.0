package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.BlockUtil;
import aethereal.ui.setting.BooleanSetting;
import aethereal.type.EntityCategory;
import aethereal.model.EntityFilter;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementUpdateEvent;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.math.SimulatedPlayer;
import aethereal.type.TargetSelectionType2;

import java.util.Objects;
import java.util.stream.IntStream;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.MaceItem;
import net.minecraft.util.hit.EntityHitResult;

@Aliases(aliases = {"Trigger Bot", "Auto Attack", "Attack Bot"})
public class TriggerBotModule extends Module {
    public static final int attackDelay = 10;
    public final MultiSelectSetting<TargetSelectionType2> targets;
    public final BooleanSetting onlyCriticalHit;
    public final BooleanSetting critsWithSpace;
    public final Mc mc;
    public int sprintStopTicks;
    public int attackCooldown;

    public TriggerBotModule() {
        super(ModuleTab.COMBAT, "Trigger Bot");
        this.targets = new MultiSelectSetting(Lang.SELECTTARGETS).values(TargetSelectionType2.class);
        this.onlyCriticalHit = new BooleanSetting(Lang.TRIGGERBOT_ONLY_CRITICAL_HIT);
        BooleanSetting class665Var = new BooleanSetting(Lang.ATTACKAURA_CRITS_WITH_SPACE, Lang.ATTACKAURA_CRITS_WITH_SPACE_DESC);
        BooleanSetting class665Var2 = this.onlyCriticalHit;
        Objects.requireNonNull(class665Var2);
        this.critsWithSpace = class665Var.visible(class665Var2::isValue);
        this.mc = Mc.INSTANCE;
        this.sprintStopTicks = 0;
        this.attackCooldown = 0;
        addSettings(this.targets, this.onlyCriticalHit, this.critsWithSpace);
        register(MovementUpdateEvent.class, class308Var -> {
            if (isState() && this.mc.isWorldLoaded() && class308Var.getDirectionalInput().isMoving() && this.sprintStopTicks > 0 && class308Var.isSprint()) {
                class308Var.setStopProgression(true);
                class308Var.setSprint(false);
            }
        }, EventPriority.HIGH);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                if (this.sprintStopTicks > 0) {
                    this.sprintStopTicks--;
                }
                if (this.attackCooldown > 0) {
                    this.attackCooldown--;
                    return;
                }
                if ((this.mc.getCrosshairTarget()) instanceof EntityHitResult crosshairTarget ) {
                    Entity entity = crosshairTarget.getEntity();
                    EntityFilter class095Var = new EntityFilter();
                    if (this.targets.isSelected(TargetSelectionType2.PLAYERS)) {
                        class095Var.add(EntityCategory.PLAYER);
                    }
                    if (this.targets.isSelected(TargetSelectionType2.FRIENDS)) {
                        class095Var.add(EntityCategory.FRIEND);
                    }
                    if (this.targets.isSelected(TargetSelectionType2.MOBS)) {
                        class095Var.add(EntityCategory.MOB);
                    }
                    if (this.targets.isSelected(TargetSelectionType2.ANIMALS)) {
                        class095Var.add(EntityCategory.ANIMAL);
                    }
                    if (class095Var.matches(entity)) {
                        if (canAttack(player, 1)) {
                            this.sprintStopTicks = 1;
                            player.setSprinting(false);
                        }
                        if (!canAttack(player, 0)) {
                            return;
                        }
                        MinecraftClient.getInstance().doAttack();
                        this.attackCooldown = attackDelay;
                    }
                }
            }
        });
    }

    public boolean canAttack(ClientPlayerEntity clientPlayerEntity, int i) {
        if (this.mc.getCurrentScreen() instanceof GenericContainerScreen) {
            return false;
        }
        return IntStream.rangeClosed(0, i).anyMatch(i2 -> {
            return canCrit(clientPlayerEntity, i2);
        });
    }

    public boolean canCrit(ClientPlayerEntity clientPlayerEntity, int i) {
        if (!(clientPlayerEntity.getAttackCooldownProgress(((float) i) + 0.5f) > 0.9f && this.attackCooldown - i <= 0)) {
            return false;
        }
        if (!this.onlyCriticalHit.isValue()) {
            return true;
        }
        SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(i);
        boolean z = class136VarSimulateLocalPlayer.input.playerInput.jump() || ((AutoJumpModule) Expensive.INSTANCE.moduleRepository().get(AutoJumpModule.class)).isState();
        if (this.critsWithSpace.isValue() && class136VarSimulateLocalPlayer.onGround && !z) {
            return true;
        }
        return hasCritCondition(clientPlayerEntity, i);
    }

    public boolean hasCritCondition(ClientPlayerEntity clientPlayerEntity, int i) {
        SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(i);
        if (class136VarSimulateLocalPlayer.hasStatusEffect(StatusEffects.LEVITATION) || class136VarSimulateLocalPlayer.hasStatusEffect(StatusEffects.BLINDNESS) || class136VarSimulateLocalPlayer.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return true;
        }
        if (BlockUtil.checkBlockIntersection(class136VarSimulateLocalPlayer.boundingBox, block -> {
            return block instanceof CobwebBlock;
        }) || class136VarSimulateLocalPlayer.isSubmergedInWater() || class136VarSimulateLocalPlayer.isInLava() || class136VarSimulateLocalPlayer.isClimbing() || this.mc.getPlayer().getAbilities().flying) {
            return true;
        }
        if (clientPlayerEntity.getMainHandStack().getItem() instanceof MaceItem) {
            return MaceItem.shouldDealAdditionalDamage(clientPlayerEntity);
        }
        if (!class136VarSimulateLocalPlayer.onGround && class136VarSimulateLocalPlayer.fallDistance > 0.0f) {
            return class136VarSimulateLocalPlayer.velocity.y > -0.3d || !(SimulatedPlayer.simulateLocalPlayer(i + 1).onGround || SimulatedPlayer.simulateLocalPlayer(i + 2).onGround);
        }
        return false;
    }
}
