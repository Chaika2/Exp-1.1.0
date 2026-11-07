package ru.expensive.mixin;

import aethereal.math.Rotation;
import aethereal.event.PlayerMoveEvent;
import aethereal.math.DirectionalInput;
import aethereal.event.OverlayEffectEvent;
import aethereal.type.OverlayEffectType;
import aethereal.event.PlayerInitEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.type.TickStage;
import aethereal.event.RespawnEvent;
import aethereal.Expensive;
import aethereal.event.PushEvent;
import aethereal.type.PushType;
import aethereal.event.ClipAtLedgeEvent;
import aethereal.event.CloseScreenEvent;
import aethereal.event.MovementInputEvent3;
import aethereal.event.MovementUpdateEvent;
import aethereal.event.MovementUpdateSource;
import aethereal.event.PlayerPositionEvent;
import aethereal.type.PlayerPositionStage;
import aethereal.util.RotationManager;
import aethereal.module.ElytraHelperModule;
import aethereal.module.AutoTotemModule;
import aethereal.module.SprintModule;
import aethereal.module.WaterFixModule;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Portal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.stat.StatHandler;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {

    @Shadow
    protected int field_3935;

    @Shadow
    public Input field_3913;

    @Shadow
    private boolean field_23093;

    @Shadow
    @Final
    protected MinecraftClient field_3937;

    @Shadow
    private int field_3934;

    @Shadow
    @Final
    public ClientPlayNetworkHandler field_3944;

    @Shadow
    private boolean field_3939;

    @Shadow
    private int field_3917;

    @Shadow
    private int field_3938;

    @Shadow
    private float field_3922;

    public ClientPlayerEntityMixin(ClientWorld clientWorld, GameProfile gameProfile) {
        super(clientWorld, gameProfile);
    }

    @Shadow
    protected abstract void method_60887(boolean z);

    @Shadow
    protected abstract void method_30673(double d, double d2);

    @Shadow
    public abstract Portal.Effect method_60886();

    @Shadow
    public abstract boolean method_20623();

    @Shadow
    public abstract boolean method_20303();

    @Shadow
    protected abstract boolean method_48300();

    @Shadow
    public abstract boolean method_46743();

    @Shadow
    protected abstract boolean method_3134();

    @Shadow
    @Nullable
    public abstract JumpingMount method_45773();

    @Shadow
    protected abstract boolean method_65949();

    @Shadow
    public abstract float method_3151();

    @Shadow
    protected abstract void method_3133();

    @Inject(at = {@At("TAIL")}, method = {"<init>"})
    private void onInit(MinecraftClient minecraftClient, ClientWorld clientWorld, ClientPlayNetworkHandler clientPlayNetworkHandler, StatHandler statHandler, ClientRecipeBook clientRecipeBook, boolean z, boolean z2, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new PlayerInitEvent());
    }

    @Inject(method = {"tick"}, at = {@At("HEAD")}, cancellable = true)
    private void tick(CallbackInfo callbackInfo) {
        PlayerTickEvent class130Var = new PlayerTickEvent(TickStage.PRE);
        Expensive.INSTANCE.eventDispatcher().dispatch(class130Var);
        if (class130Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"tick"}, at = {@At("RETURN")}, cancellable = true)
    private void postTickHook(CallbackInfo callbackInfo) {
        PlayerTickEvent class130Var = new PlayerTickEvent(TickStage.POST);
        Expensive.INSTANCE.eventDispatcher().dispatch(class130Var);
        if (class130Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"requestRespawn"}, at = {@At("HEAD")})
    private void requestRespawn(CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new RespawnEvent());
    }

    @Inject(method = {"move"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V")}, cancellable = true)
    public void move(MovementType movementType, Vec3d vec3d, CallbackInfo callbackInfo) {
        PlayerMoveEvent class039Var = new PlayerMoveEvent(vec3d);
        Expensive.INSTANCE.eventDispatcher().dispatch(class039Var);
        if (class039Var.isCancelled()) {
            super.move(movementType, class039Var.movement());
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"pushOutOfBlocks"}, at = {@At("HEAD")}, cancellable = true)
    public void pushOutOfBlocks(double d, double d2, CallbackInfo callbackInfo) {
        PushEvent class231Var = new PushEvent(PushType.BLOCKS);
        Expensive.INSTANCE.eventDispatcher().dispatch(class231Var);
        if (class231Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"closeHandledScreen"}, at = {@At("HEAD")}, cancellable = true)
    private void closeHandledScreenHook(CallbackInfo callbackInfo) {
        CloseScreenEvent class270Var = new CloseScreenEvent(this.field_3937.currentScreen);
        Expensive.INSTANCE.eventDispatcher().dispatch(class270Var);
        if (class270Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(method = {"tickMovement"}, at = {@At("HEAD")}, cancellable = true)
    public void tickMovement(CallbackInfo callbackInfo) {
        if (this.field_3935 > 0) {
            this.field_3935--;
        }
        if (!(this.field_3937.currentScreen instanceof DownloadingTerrainScreen)) {
            method_60887(method_60886() == Portal.Effect.CONFUSION);
            tickPortalCooldown();
        }
        boolean z = this.field_3913.playerInput.jump() && !((ElytraHelperModule) Expensive.INSTANCE.moduleRepository().get(ElytraHelperModule.class)).canStart();
        boolean zSneak = this.field_3913.playerInput.sneak();
        boolean zMethod_20623 = method_20623();
        PlayerAbilities abilities = getAbilities();
        this.field_23093 = (abilities.flying || isSwimming() || hasVehicle() || !canChangeIntoPose(EntityPose.CROUCHING) || (!isSneaking() && (isSleeping() || canChangeIntoPose(EntityPose.STANDING)))) ? false : true;
        this.field_3913.tick();
        this.field_3937.getTutorialManager().onMovement(this.field_3913);
        if (method_65949()) {
            setSprinting(false);
        }
        if (isUsingItem() && !hasVehicle()) {
            MovementInputEvent3 class289Var = new MovementInputEvent3(this.field_3913.movementForward, this.field_3913.movementSideways);
            Expensive.INSTANCE.eventDispatcher().dispatch(class289Var);
            if (!class289Var.isCancelled()) {
                this.field_3913.movementSideways *= 0.2f;
                this.field_3913.movementForward *= 0.2f;
                this.field_3935 = 0;
            }
        }
        if (method_20303()) {
            float attributeValue = (float) getAttributeValue(EntityAttributes.SNEAKING_SPEED);
            Input input = this.field_3913;
            input.movementSideways *= attributeValue;
            input.movementForward *= attributeValue;
        }
        boolean z2 = false;
        if (this.field_3934 > 0) {
            this.field_3934--;
            z2 = true;
            this.field_3913.jump();
        }
        if (!this.noClip) {
            method_30673(getX() - (((double) getWidth()) * 0.35d), getZ() + (((double) getWidth()) * 0.35d));
            method_30673(getX() - (((double) getWidth()) * 0.35d), getZ() - (((double) getWidth()) * 0.35d));
            method_30673(getX() + (((double) getWidth()) * 0.35d), getZ() - (((double) getWidth()) * 0.35d));
            method_30673(getX() + (((double) getWidth()) * 0.35d), getZ() + (((double) getWidth()) * 0.35d));
        }
        if (zSneak) {
            this.field_3935 = 0;
        }
        MovementUpdateEvent class308Var = new MovementUpdateEvent(DirectionalInput.fromInput(this.field_3913.playerInput), this.field_3937.options.sprintKey.isPressed(), MovementUpdateSource.MOVEMENT_TICK);
        Expensive.INSTANCE.eventDispatcher().dispatch(class308Var);
        boolean zMethod_48300 = method_48300();
        SprintModule sprintModule = (SprintModule) Expensive.INSTANCE.moduleRepository().get(SprintModule.class);
        boolean zIsOnGround = hasVehicle() ? getVehicle().isOnGround() : isOnGround();
        // В воде и лаве играем строго по ванили. Флаги Sprint ("игнорировать
        // плавание / использование предмета / горизонтальную коллизию") меняют
        // позу и скорость так, что клиент перестаёт совпадать с серверным
        // расчётом воды, и античит пишет "FastMath ... неверное движение".
        // На суше поведение модуля не меняется.
        boolean inFluid = isTouchingWater() || isSwimming() || isInLava();
        // Ограничение воды (модуль Water Fix) можно выключить - тогда клиент
        // снова получает обычный ванильный спринт в воде, но и предупреждение
        // античита вернётся. По умолчанию ограничение включено.
        WaterFixModule waterFix = Expensive.INSTANCE.moduleRepository().find(WaterFixModule.class).orElse(null);
        boolean limitWaterSpeed = waterFix != null && waterFix.isWaterSpeedLimited();
        boolean waterPhysics = inFluid && limitWaterSpeed;
        boolean moduleSprint = sprintModule.isState() && !waterPhysics;
        boolean z3 = moduleSprint
                ? (((zSneak && !sprintModule.ignoreSneak()) || zMethod_20623) ? false : true)
                : (!zSneak && !zMethod_20623);
        boolean itemUseSprint = moduleSprint ? (!isUsingItem() || sprintModule.ignoreItemUse()) : !isUsingItem();
        // В воде Sprint-модуль не должен подмешивать свой форс-спринт через событие:
        // берём именно ванильное состояние клавиши.
        // Спринт в жидкости выключен полностью: 1.21.4 в воде со спринтом держит
        // drag 0.9 вместо 0.8 и разгоняется до ~0.19 блока/тик, а античит
        // mlegacy.ru считает воду по старой формуле (0.8 + Depth Strider),
        // то есть ждёт ~0.08. Именно этот разрыв и даёт "FastMath ... неверное
        // движение" - предупреждение приходило ровно на сериях плавания 0.19.
        // Без спринта и без Depth Strider вода ровно такая, как у ванильного
        // игрока без ботинок, и prediction сходится.
        boolean sprintRequested = !waterPhysics
                && (moduleSprint ? class308Var.isSprint() : this.field_3937.options.sprintKey.isPressed());
        if (!waterPhysics) {
            if ((zIsOnGround || isSubmergedInWater()) && z3 && zMethod_48300 && itemUseSprint) {
                if (this.field_3935 > 0 || sprintRequested) {
                    setSprinting(true);
                } else {
                    this.field_3935 = 7;
                }
            }
            if ((!isTouchingWater() || isSubmergedInWater()) && zMethod_48300 && sprintRequested && itemUseSprint) {
                setSprinting(true);
            }
        }
        if (isSprinting()) {
            MovementUpdateEvent class308Var2 = new MovementUpdateEvent(DirectionalInput.fromInput(this.field_3913.playerInput), method_46743(), MovementUpdateSource.MOVEMENT_TICK);
            Expensive.INSTANCE.eventDispatcher().dispatch(class308Var2);
            boolean canSprintNow = moduleSprint ? class308Var2.isSprint() : method_46743();
            boolean z4 = (this.field_3913.hasForwardMovement() && canSprintNow) ? false : true;
            boolean z5 = z4
                    || (this.horizontalCollision && !this.collidedSoftly && (!moduleSprint || !sprintModule.ignoreHorizontalCollision()))
                    || (isTouchingWater() && !isSubmergedInWater());
            if (isSwimming()) {
                boolean zSwimStop = (!isOnGround() && !this.field_3913.playerInput.sneak() && z4) || !isTouchingWater();
                if (zSwimStop && (!moduleSprint || !sprintModule.ignoreCrawl())) {
                    setSprinting(false);
                }
            } else if (z5) {
                setSprinting(false);
            }
        }
        // Спринт, «прилипший» с суши (вошли в воду на бегу), в жидкости гасим
        // в этом же тике: sendMovementPackets уйдёт уже со STOP_SPRINTING и
        // нулевым sprint-битом PlayerInput, поэтому сервер посчитает воду по
        // не-спринтовой формуле и не увидит расхождения скорости.
        if (waterPhysics && isSprinting()) {
            setSprinting(false);
        }
        boolean z6 = false;
        if (abilities.allowFlying) {
            if (this.field_3937.interactionManager.isFlyingLocked()) {
                if (!abilities.flying) {
                    abilities.flying = true;
                    z6 = true;
                    sendAbilitiesUpdate();
                }
            } else if (!z && this.field_3913.playerInput.jump() && !z2) {
                if (this.abilityResyncCountdown == 0) {
                    this.abilityResyncCountdown = 7;
                } else if (!isSwimming()) {
                    abilities.flying = !abilities.flying;
                    if (abilities.flying && isOnGround()) {
                        jump();
                    }
                    z6 = true;
                    sendAbilitiesUpdate();
                    this.abilityResyncCountdown = 0;
                }
            }
        }
        if (this.field_3913.playerInput.jump() && !z6 && !z && !isClimbing() && checkGliding()) {
            this.field_3944.sendPacket(new ClientCommandC2SPacket(this, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
        this.field_3939 = isGliding();
        if (isTouchingWater() && this.field_3913.playerInput.sneak() && shouldSwimInFluids()) {
            knockDownwards();
        }
        if (isSubmergedIn(FluidTags.WATER)) {
            this.field_3917 = MathHelper.clamp(this.field_3917 + (isSpectator() ? 10 : 1), 0, 600);
        } else if (this.field_3917 > 0) {
            isSubmergedIn(FluidTags.WATER);
            this.field_3917 = MathHelper.clamp(this.field_3917 - 10, 0, 600);
        }
        if (abilities.flying && method_3134()) {
            int i = 0;
            if (this.field_3913.playerInput.sneak()) {
                i = 0 - 1;
            }
            if (this.field_3913.playerInput.jump()) {
                i++;
            }
            if (i != 0) {
                setVelocity(getVelocity().add(0.0d, i * abilities.getFlySpeed() * 3.0f, 0.0d));
            }
        }
        JumpingMount jumpingMountMethod_45773 = method_45773();
        if (jumpingMountMethod_45773 == null || jumpingMountMethod_45773.getJumpCooldown() != 0) {
            this.field_3922 = 0.0f;
        } else {
            if (this.field_3938 < 0) {
                this.field_3938++;
                if (this.field_3938 == 0) {
                    this.field_3922 = 0.0f;
                }
            }
            if (z && !this.field_3913.playerInput.jump()) {
                this.field_3938 = -10;
                jumpingMountMethod_45773.setJumpStrength(MathHelper.floor(method_3151() * 100.0f));
                method_3133();
            } else if (!z && this.field_3913.playerInput.jump()) {
                this.field_3938 = 0;
                this.field_3922 = 0.0f;
            } else if (z) {
                this.field_3938++;
                if (this.field_3938 < 10) {
                    this.field_3922 = this.field_3938 * 0.1f;
                } else {
                    this.field_3922 = 0.8f + ((2.0f / (this.field_3938 - 9)) * 0.1f);
                }
            }
        }
        // Окно сглаживания вокруг клика по оффхенду. Точка выбрана перед
        // super.tickMovement(): KeyboardInput к этому моменту уже собрал input,
        // а LivingEntity ещё не считал по нему перемещение - значит множитель
        // реально влияет на скорость, и игрок продолжает идти, просто медленно.
        // Спринт здесь же гасится, поэтому STOP_SPRINTING уходит из
        // sendMovementPackets() этого же тика и сервер успевает узнать о паузе
        // ДО клика, который уходит следующим тиком из PlayerTickEvent.PRE.
        float fSprintSlowdown = AutoTotemModule.consumeSprintSlowdownTick((ClientPlayerEntity)(Object)this);
        if (fSprintSlowdown < 1.0f) {
            this.field_3913.movementForward *= fSprintSlowdown;
            this.field_3913.movementSideways *= fSprintSlowdown;
        }
        super.tickMovement();
        if (isOnGround() && abilities.flying && !this.field_3937.interactionManager.isFlyingLocked()) {
            abilities.flying = false;
            sendAbilitiesUpdate();
        }
        callbackInfo.cancel();
    }

    // Обход правила сервера "смена состояния бега рядом с кликом по инвентарю".
    //
    // sendSprintingPacket() в 1.21.4 устроен так:
    //     boolean bl = isSprinting();
    //     if (bl != this.lastSprinting) {
    //         this.lastSprinting = bl;
    //         sendPacket(new ClientCommandC2SPacket(this, START/STOP_SPRINTING));
    //     }
    // и вызывается первым же из sendMovementPackets(). Отменяя метод, мы
    // не обновляем lastSprinting, поэтому пакет не пропадает, а просто уходит
    // следующим тиком: в тике клика сервер не видит никакой смены состояния.
    // Ни спринт ауры, ни SprintStopEvent при этом не затронуты.
    // Тот же приём использует Zenith (@Redirect по sendSprintingPacket).
    //
    // Подавление привязано к age тика, а не к счётчику вызовов: в тике sendSprintingPacket
    // зовётся из двух мест (отдельный вызов перед sendMovementPackets и первый же
    // внутри неё), и нужно глушить оба, но строго в одном тике - иначе уедет
    // настоящая смена бега на следующем тике. Ограничение сверху нужно, чтобы клиент
    // не разошёлся с сервером (максимум 4 пакета за тик - это с запасом).
    @Inject(method = {"sendSprintingPacket"}, at = {@At("HEAD")}, cancellable = true)
    private void suppressSprintPacket(CallbackInfo callbackInfo) {
        if (this.age == AutoTotemModule.suppressSprintPacketTickAge && AutoTotemModule.sprintSuppressedCount < 4) {
            AutoTotemModule.sprintSuppressedCount++;
            callbackInfo.cancel();
        }
    }

    // Пауза спринта на тик клика (путь AutoPotion, где клик нельзя отложить).
    //
    // Гасить спринт в PlayerTickEvent.PRE бессмысленно: sendMovementPackets()
    // вызывается позже, уже после player.tickMovement(), где KeyboardInput заново
    // собирает input.playerInput из клавиш и ванильный setSprinting() снова включает
    // спринт - к моменту формирования пакетов он был бы включён, и STOP_SPRINTING ушёл
    // бы в одном тике с кликом (ровно то, что палится). Здесь последняя точка перед
    // sendSprintingPacket(), поэтому спринт гасится "на миллисекунду" - локально, без
    // пакета на сервер: sendSprintingPacket() рядом отменяется, lastSprinting не
    // меняется, и сервер всё это время считает нас бегущими. На resumeAge спринт
    // включается обратно, пакета не будет - сервер уже бегущий.
    // Для автототема этот механизм НЕ используется: там клик откладывается на тик
    // (AutoTotemModule.canSwapNow), чтобы STOP_SPRINTING реально ушёл на сервер.
    @Inject(method = {"sendMovementPackets"}, at = {@At("HEAD")})
    private void sprintPauseForClick(CallbackInfo callbackInfo) {
        if (this.age == AutoTotemModule.pauseSprintTickAge) {
            this.setSprinting(false);
        } else if (this.age == AutoTotemModule.pauseSprintResumeAge) {
            this.setSprinting(true);
        }
    }

    @ModifyExpressionValue(method = {"isBlind"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z")})
    private boolean hookSprintIgnoreBlindness(boolean z) {
        OverlayEffectEvent class069Var = new OverlayEffectEvent(OverlayEffectType.BLIDNESS);
        Expensive.INSTANCE.eventDispatcher().dispatch(class069Var);
        return !class069Var.isCancelled() && z;
    }

    @ModifyExpressionValue(method = {"canSprint"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/entity/player/HungerManager;getFoodLevel()I")})
    private int canSprintHook(int i) {
        OverlayEffectEvent class069Var = new OverlayEffectEvent(OverlayEffectType.HUNGER);
        Expensive.INSTANCE.eventDispatcher().dispatch(class069Var);
        if (class069Var.isCancelled()) {
            return 20;
        }
        return i;
    }

    @Inject(method = {"sendMovementPackets"}, at = {@At("HEAD")}, cancellable = true)
    private void sendMovementPackets(CallbackInfo callbackInfo) {
        PlayerPositionEvent class316Var = new PlayerPositionEvent(PlayerPositionStage.PRE, getX(), getY(), getZ(), getYaw(), getPitch(), isOnGround());
        Expensive.INSTANCE.eventDispatcher().dispatch(class316Var);
        if (class316Var.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getYaw()F")})
    private float hookSilentRotationYaw(float f) {
        Rotation currentRotationOriginal = RotationManager.INSTANCE.getCurrentRotationOriginal();
        return currentRotationOriginal == null ? f : currentRotationOriginal.getYaw();
    }

    @ModifyExpressionValue(method = {"sendMovementPackets", "tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getPitch()F")})
    private float hookSilentRotationPitch(float f) {
        Rotation currentRotationOriginal = RotationManager.INSTANCE.getCurrentRotationOriginal();
        return currentRotationOriginal == null ? f : currentRotationOriginal.getPitch();
    }

    @Inject(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;sendMovementPackets()V", shift = At.Shift.AFTER)})
    private void postSendMovementPackets(CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new PlayerPositionEvent(PlayerPositionStage.POST, getX(), getY(), getZ(), getYaw(), getPitch(), isOnGround()));
    }

    public boolean clipAtLedge() {
        ClipAtLedgeEvent class250Var = new ClipAtLedgeEvent(super.clipAtLedge());
        Expensive.INSTANCE.eventDispatcher().dispatch(class250Var);
        return class250Var.isClip();
    }
}
