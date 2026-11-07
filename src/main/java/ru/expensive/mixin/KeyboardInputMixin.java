package ru.expensive.mixin;

import aethereal.event.KeyBindingEvent;
import aethereal.event.MovementInputEvent;
import aethereal.math.DirectionalInput;
import aethereal.Expensive;
import aethereal.event.MovementUpdateEvent;
import aethereal.event.MovementUpdateSource;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({KeyboardInput.class})
public abstract class KeyboardInputMixin extends InputMixin {

    @Shadow
    @Final
    private GameOptions field_3902;

    @WrapOperation(method = {"tick"}, at = {@At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z")})
    private boolean hookPressed(KeyBinding keyBinding, Operation<Boolean> operation) {
        KeyBindingEvent class035Var = new KeyBindingEvent(keyBinding);
        Expensive.INSTANCE.eventDispatcher().dispatch(class035Var);
        return ((Boolean) operation.call(new Object[]{keyBinding})).booleanValue() || class035Var.isCancelled();
    }

    @ModifyExpressionValue(method = {"tick"}, at = {@At(value = "NEW", target = "(ZZZZZZZ)Lnet/minecraft/util/PlayerInput;")})
    private PlayerInput modifyInput(PlayerInput playerInput) {
        MovementInputEvent class040Var = new MovementInputEvent(DirectionalInput.fromInput(playerInput), playerInput.jump(), playerInput.sneak(), playerInput.sprint());
        Expensive.INSTANCE.eventDispatcher().dispatch(class040Var);
        DirectionalInput class041VarTransformDirection = correctMovementInput(class040Var.getInput());
        MovementUpdateEvent class308Var = new MovementUpdateEvent(class041VarTransformDirection, playerInput.sprint(), MovementUpdateSource.INPUT);
        Expensive.INSTANCE.eventDispatcher().dispatch(class308Var);
        // В воде/лаве sprint-бит PlayerInputC2SPacket обязан быть false: клиент
        // там принципиально не бежит (см. ClientPlayerEntityMixin.tickMovement),
        // а 1.21.4 при спринте в воде держит drag 0.9 вместо 0.8 и уезжает
        // вдвое быстрее серверной модели. Любое расхождение флага и скорости
        // даёт "FastMath ... неверное движение".
        ClientPlayerEntity fluidPlayer = MinecraftClient.getInstance().player;
        boolean inFluid = fluidPlayer != null
                && (fluidPlayer.isTouchingWater() || fluidPlayer.isSwimming() || fluidPlayer.isInLava());
        aethereal.module.WaterFixModule waterFix = Expensive.INSTANCE.moduleRepository()
                .find(aethereal.module.WaterFixModule.class).orElse(null);
        boolean limitWaterSpeed = waterFix != null && waterFix.isWaterSpeedLimited();
        boolean sprintFlag = (inFluid && limitWaterSpeed) ? false : class308Var.isSprint();
        return new PlayerInput(class041VarTransformDirection.forward(), class041VarTransformDirection.backward(), class041VarTransformDirection.left(), class041VarTransformDirection.right(), class040Var.isJumping(), class040Var.isSneaking(), sprintFlag);
    }

    @Unique
    private DirectionalInput correctMovementInput(DirectionalInput class041Var) {
        // Пересчёт WASD в ближайшее из восьми направлений убран полностью.
        //
        // Раньше STRICT каждый тик подбирал комбинацию клавиш так, чтобы мировое
        // направление движения совпадало с камерой при подменённом угле ауры.
        // На границах направлений (каждые 45 градусов) комбинация перекидывалась
        // каждый тик, а в воздухе это складывалось в почти нулевое горизонтальное
        // смещение - игрок "подвисал" и терял скорость. На сервере 1.16.5 биты
        // входа вообще не передаются, поэтому выгоды от пересчёта не было.
        //
        // Теперь режимы различаются только тем, каким углом считается физика
        // (см. ScheduledRotation.usesYawCorrection), а вход всегда остаётся тем,
        // который нажал игрок.
        return class041Var;
    }
}
