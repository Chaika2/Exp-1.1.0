package aethereal.util;
import aethereal.event.ClientTickEvent;
import aethereal.type.CombatPauseManager;
import aethereal.type.EventPhase;
import aethereal.Expensive;
import aethereal.event.JumpEvent;
import aethereal.type.Mc;
import aethereal.module.Module;
import aethereal.event.MovementYawEvent;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.math.Rotation;
import aethereal.config.RotationConfig;
import aethereal.math.RotationMath;
import aethereal.event.RotationUpdateEvent;
import aethereal.math.RotationVector;
import aethereal.event.RotationVectorEvent;
import aethereal.math.ScheduledRotation;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationManager {
    public static final RotationManager INSTANCE = new RotationManager();
    public final TickScheduler<ScheduledRotation> scheduler = new TickScheduler<>();

    public ScheduledRotation lastStrategy;
    public Rotation currentRotationOriginal;
    public Rotation previousRotation;
    public Rotation serverRotation;

    public RotationManager() {
        Expensive.INSTANCE.eventDispatcher().register(PacketSendEvent.class, class037Var -> {
            if (class037Var.isCancelled()) {
                return;
            }
            net.minecraft.network.packet.Packet<?> packet = class037Var.getPacket();
            Rotation class007VarNormalize = null;
            if (packet instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket playerMoveC2SPacket = (PlayerMoveC2SPacket) packet;
                if (playerMoveC2SPacket.changesLook()) {
                    class007VarNormalize = new Rotation(playerMoveC2SPacket.getYaw(1.0f), playerMoveC2SPacket.getPitch(1.0f)).normalize();
                }
            }
            if (packet instanceof PlayerInteractItemC2SPacket) {
                PlayerInteractItemC2SPacket playerInteractItemC2SPacket = (PlayerInteractItemC2SPacket) packet;
                class007VarNormalize = new Rotation(playerInteractItemC2SPacket.getYaw(), playerInteractItemC2SPacket.getPitch()).normalize();
            }
            this.serverRotation = class007VarNormalize;
        });
        Expensive.INSTANCE.eventDispatcher().register(PacketReceiveEvent.class, class051Var -> {
            if (class051Var.isCancelled()) {
                return;
            }
            if ((class051Var.getPacket()) instanceof PlayerPositionLookS2CPacket packet ) {
                PlayerPositionLookS2CPacket playerPositionLookS2CPacket = packet;
                this.serverRotation = new Rotation(playerPositionLookS2CPacket.change().yaw(), playerPositionLookS2CPacket.change().pitch()).normalize();
            }
        });
        Expensive.INSTANCE.eventDispatcher().register(ClientTickEvent.class, class181Var -> {
            Expensive.INSTANCE.eventDispatcher().dispatch(new RotationUpdateEvent(EventPhase.PRE));
            updateRotation();
            Expensive.INSTANCE.eventDispatcher().dispatch(new RotationUpdateEvent(EventPhase.POST));
        });
        Expensive.INSTANCE.eventDispatcher().register(RotationVectorEvent.class, class150Var -> {
            ScheduledRotation currentStrategy = getCurrentStrategy();
            Rotation class007Var = this.currentRotationOriginal;
            if (currentStrategy == null || class007Var == null) {
                return;
            }
            class150Var.setYaw(class007Var.getYaw());
            class150Var.setPitch(class007Var.getPitch());
        });
        Expensive.INSTANCE.eventDispatcher().register(MovementYawEvent.class, class257Var -> {
            ScheduledRotation currentStrategy = getCurrentStrategy();
            Rotation class007Var = this.currentRotationOriginal;
            if (currentStrategy == null || class007Var == null || !currentStrategy.moveCorrection()) {
                return;
            }
            class257Var.setYaw(class007Var.getYaw());
        });
        Expensive.INSTANCE.eventDispatcher().register(JumpEvent.class, class237Var -> {
            ScheduledRotation currentStrategy = getCurrentStrategy();
            Rotation class007Var = this.currentRotationOriginal;
            if (currentStrategy == null || class007Var == null || !currentStrategy.moveCorrection()) {
                return;
            }
            class237Var.setYaw(class007Var.getYaw());
        });
    }

    public Rotation getPreviousRotation() {
        return (this.currentRotationOriginal == null || this.previousRotation == null) ? new Rotation(Mc.INSTANCE.getPlayer().prevYaw, Mc.INSTANCE.getPlayer().prevPitch) : this.previousRotation;
    }

    public Rotation getServerRotation() {
        return this.serverRotation != null ? this.serverRotation : Rotation.playerRotation();
    }

    public Rotation getCurrentRotation() {
        return this.currentRotationOriginal != null ? this.currentRotationOriginal : Rotation.playerRotation();
    }

    public Rotation getMoveRotation() {
        ScheduledRotation currentStrategy = getCurrentStrategy();
        return (this.currentRotationOriginal == null || currentStrategy == null || !currentStrategy.moveCorrection()) ? Rotation.playerRotation() : this.currentRotationOriginal;
    }

    public Rotation getCurrentRotationOriginal() {
        return this.currentRotationOriginal;
    }

    public Rotation getInterpolatedRotation() {
        return MathUtil.interpolate(getPreviousRotation(), getCurrentRotation());
    }

    public void rotateTo(Vec3d vec3d, Module class605Var, ClientPlayerEntity clientPlayerEntity, int i) {
        scheduleRotation(RotationMath.INSTANCE.fromVec3d(vec3d.subtract(clientPlayerEntity.getEyePos())), RotationConfig.LINEAR_WITH_FOCUSED_CORRECTION, 2, (Module) null, i);
    }

    public void clear() {
        this.scheduler.activeTasks.clear();
        this.scheduler.tickCounter = 0;
    }

    public void cancelTasksFor(Module class605Var) {
        this.scheduler.cancel(class605Var);
    }

    public void scheduleRotation(RotationVector class390Var, LivingEntity livingEntity, RotationConfig class008Var, int i, Module class605Var, int i2) {
        scheduleRotation(class008Var.createRotationStrategy(class390Var.rotation(), class390Var.vec(), livingEntity, i2), i, class605Var);
    }

    public void scheduleRotation(Rotation class007Var, RotationConfig class008Var, int i, Module class605Var, int i2) {
        scheduleRotation(class008Var.createRotationStrategy(class007Var, i2), i, class605Var);
    }

    public void scheduleRotation(RotationVector class390Var, LivingEntity livingEntity, RotationConfig class008Var, int i, int i2, Module class605Var) {
        scheduleRotation(class008Var.createRotationStrategy(class390Var.rotation(), class390Var.vec(), livingEntity, i2), i, class605Var);
    }

    public void scheduleRotation(Rotation class007Var, RotationConfig class008Var, int i, int i2, Module class605Var) {
        scheduleRotation(class008Var.createRotationStrategy(class007Var, i2), i, class605Var);
    }

    public void scheduleRotation(ScheduledRotation class399Var, int i, Module class605Var) {
        if (CombatPauseManager.INSTANCE.shouldPauseRotation()) {
            return;
        }
        this.scheduler.addTask(new ScheduledTask<>(1, i, class605Var, class399Var));
    }

    public void scheduleRotation(ScheduledRotation class399Var, int i, Module class605Var, int i2) {
        if (CombatPauseManager.INSTANCE.shouldPauseRotation()) {
            return;
        }
        this.scheduler.addTask(new ScheduledTask<>(1, i, class605Var, class399Var));
    }

    public void updateRotation() {
        ScheduledRotation currentStrategy;
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null || (currentStrategy = getCurrentStrategy()) == null || CombatPauseManager.INSTANCE.shouldPauseRotation()) {
            return;
        }
        Rotation class007Var = new Rotation(player.getYaw(), player.getPitch());
        Rotation class007VarNormalize = currentStrategy.towards(this.currentRotationOriginal != null ? this.currentRotationOriginal : class007Var, this.scheduler.getValue() == null).normalize();
        float fAngleTo = class007VarNormalize.angleTo(class007Var);
        int i = this.scheduler.tickCounter;
        int iTicksUntilReset = currentStrategy.ticksUntilReset();
        if (this.lastStrategy == null || this.scheduler.getValue() != null || iTicksUntilReset > i || (fAngleTo >= 2.0f && !currentStrategy.clientRotation())) {
            setRotation(class007VarNormalize);
            this.lastStrategy = currentStrategy;
            if (currentStrategy.clientRotation()) {
                player.prevPitch = player.getPitch();
                player.prevYaw = player.getYaw();
                player.setYaw(class007VarNormalize.getYaw());
                player.setPitch(class007VarNormalize.getPitch());
            }
            this.scheduler.tick(1);
            return;
        }
        if (this.currentRotationOriginal != null) {
            float fWithFixedYaw = class007Var.withFixedYaw(this.currentRotationOriginal);
            ClientPlayerEntity player2 = Mc.INSTANCE.getPlayer();
            // withFixedYaw конгруэнтен текущему yaw по модулю 360, то есть отличается от
            // него на 0, +360 или -360. Направление взгляда одно и то же, но число yaw
            // прыгает: ClientPlayerEntity.tickNewAi сглаживает renderYaw без нормализации
            // (renderYaw += (yaw - renderYaw) * 0.5), а HeldItemRenderer крутит вьюмодель
            // на (yaw - lerp(lastRenderYaw, renderYaw)) * 0.1. Из-за прыжка на 360 рука
            // на кадр уезжает к краю экрана и возвращается - заметно, когда цель за спиной
            // и угол ауры лежит по другую сторону шва +-180.
            // Берём представитель, ближайший к текущему yaw, чтобы число осталось прежним.
            float fYaw = player2.getYaw() + MathHelper.wrapDegrees(fWithFixedYaw - player2.getYaw());
            player2.setYaw(fYaw);
            player2.prevYaw = fYaw;
        }
        clear();
        this.lastStrategy = null;
        this.currentRotationOriginal = null;
    }

    public void setRotation(Rotation class007Var) {
        if (class007Var == null) {
            ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
            this.previousRotation = this.currentRotationOriginal != null ? this.currentRotationOriginal : new Rotation(player.getYaw(), player.getPitch());
        } else {
            this.previousRotation = this.currentRotationOriginal;
        }
        this.currentRotationOriginal = class007Var;
    }

    public ScheduledRotation getCurrentStrategy() {
        return this.scheduler.getValue() != null ? this.scheduler.getValue() : this.lastStrategy;
    }

    public static double computeRotationDifference(Rotation class007Var, Rotation class007Var2) {
        return Math.hypot(Math.abs(computeAngleDifference(class007Var.getYaw(), class007Var2.getYaw())), Math.abs(class007Var.getPitch() - class007Var2.getPitch()));
    }

    public static float computeAngleDifference(float f, float f2) {
        return MathHelper.wrapDegrees(f - f2);
    }

    public TickScheduler<ScheduledRotation> getScheduler() {
        return this.scheduler;
    }
}
