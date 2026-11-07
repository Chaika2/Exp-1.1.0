package aethereal.math;
import aethereal.type.Mc;
import aethereal.type.MovementInputState;
import aethereal.model.PositionedTickable;

import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SimulatedPlayer implements PositionedTickable {
    public final PlayerEntity player;
    public final MovementInputState input;
    public Vec3d pos;
    public Vec3d velocity;
    public Box boundingBox;
    public float yaw;
    public float pitch;
    public boolean sprinting;
    public float fallDistance;
    public int jumpingCooldown;
    public boolean isJumping;
    public boolean isFallFlying;
    public boolean onGround;
    public boolean horizontalCollision;
    public boolean verticalCollision;
    public boolean touchingWater;
    public boolean isSwimming;
    public boolean submergedInWater;
    public final Object2DoubleMap<TagKey<Fluid>> fluidHeight;

    public final HashSet<TagKey<Fluid>> submergedFluidTags;
    public int tickCount = 0;
    public boolean ledgeClipped = false;
    public static final double halfBlock = 0.5d;

    public SimulatedPlayer(PlayerEntity playerEntity, MovementInputState class137Var, Vec3d vec3d, Vec3d vec3d2, Box box, float f, float f2, boolean z, float f3, int i, boolean z2, boolean z3, boolean z4, boolean z5, boolean z6, boolean z7, boolean z8, boolean z9, Object2DoubleMap<TagKey<Fluid>> object2DoubleMap, HashSet<TagKey<Fluid>> hashSet) {
        this.player = playerEntity;
        this.input = class137Var;
        this.pos = vec3d;
        this.velocity = vec3d2;
        this.boundingBox = box;
        this.yaw = f;
        this.pitch = f2;
        this.sprinting = z;
        this.fallDistance = f3;
        this.jumpingCooldown = i;
        this.isJumping = z2;
        this.isFallFlying = z3;
        this.onGround = z4;
        this.horizontalCollision = z5;
        this.verticalCollision = z6;
        this.touchingWater = z7;
        this.isSwimming = z8;
        this.submergedInWater = z9;
        this.fluidHeight = object2DoubleMap;
        this.submergedFluidTags = hashSet;
    }

    public static SimulatedPlayer simulateLocalPlayer(int i) {
        SimulatedPlayer class136VarFromClientPlayer = fromClientPlayer(MovementInputState.fromClientPlayer(Mc.INSTANCE.getPlayer().input.playerInput));
        for (int i2 = 0; i2 < i; i2++) {
            class136VarFromClientPlayer.tick();
        }
        return class136VarFromClientPlayer;
    }

    public static SimulatedPlayer simulateOtherPlayer(PlayerEntity playerEntity, int i) {
        SimulatedPlayer class136VarFromOtherPlayer = fromOtherPlayer(playerEntity, MovementInputState.guessInput(playerEntity));
        for (int i2 = 0; i2 < i; i2++) {
            class136VarFromOtherPlayer.tick();
        }
        return class136VarFromOtherPlayer;
    }

    public static SimulatedPlayer fromClientPlayer(MovementInputState class137Var) {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        return new SimulatedPlayer(player, class137Var, player.getPos(), player.getVelocity(), player.getBoundingBox(), player.getYaw(), player.getPitch(), player.isSprinting(), player.fallDistance, player.jumpingCooldown, player.jumping, player.isGliding(), player.isOnGround(), player.horizontalCollision, player.verticalCollision, player.isTouchingWater(), player.isSwimming(), player.isSubmergedInWater(), new Object2DoubleArrayMap(player.fluidHeight), new HashSet(player.submergedFluidTag));
    }

    public static SimulatedPlayer fromOtherPlayer(PlayerEntity playerEntity, MovementInputState class137Var) {
        Vec3d vec3dSubtract = playerEntity.getPos().subtract(new Vec3d(playerEntity.prevX, playerEntity.prevY, playerEntity.prevZ));
        return new SimulatedPlayer(playerEntity, class137Var, playerEntity.getPos(), vec3dSubtract, playerEntity.getBoundingBox(), vec3dSubtract.horizontalLengthSquared() > 1.0E-6d ? (float) Math.toDegrees(Math.atan2(-vec3dSubtract.x, vec3dSubtract.z)) : playerEntity.getYaw(), playerEntity.getPitch(), playerEntity.isSprinting(), playerEntity.fallDistance, playerEntity.jumpingCooldown, playerEntity.jumping, playerEntity.isGliding(), playerEntity.isOnGround(), playerEntity.horizontalCollision, playerEntity.verticalCollision, playerEntity.isTouchingWater(), playerEntity.isSwimming(), playerEntity.isSubmergedInWater(), new Object2DoubleArrayMap(playerEntity.fluidHeight), new HashSet(playerEntity.submergedFluidTag));
    }

    @Override
    public Vec3d pos() {
        return this.pos;
    }

    @Override
    public void tick() {
        this.tickCount++;
        this.ledgeClipped = false;
        if (this.pos.y <= -70.0d) {
            return;
        }
        this.input.update();
        updateTouchingWater();
        updateSubmergedFluids();
        updateSwimming();
        if (this.jumpingCooldown > 0) {
            this.jumpingCooldown--;
        }
        this.isJumping = this.input.playerInput.jump();
        double d = this.velocity.x;
        double d2 = this.velocity.y;
        double d3 = this.velocity.z;
        if (Math.abs(this.velocity.x) < 0.003d) {
            d = 0.0d;
        }
        if (Math.abs(this.velocity.y) < 0.003d) {
            d2 = 0.0d;
        }
        if (Math.abs(this.velocity.z) < 0.003d) {
            d3 = 0.0d;
        }
        if (this.onGround) {
            this.isFallFlying = false;
        }
        this.velocity = new Vec3d(d, d2, d3);
        if (this.isJumping) {
            double dMethod015 = isInLava() ? getFluidHeight(FluidTags.LAVA) : getFluidHeight(FluidTags.WATER);
            boolean z = isTouchingWater() && dMethod015 > 0.0d;
            double dMethod005 = getSwimHeight();
            if (z && (!this.onGround || dMethod015 > dMethod005)) {
                swimUpward(FluidTags.WATER);
            } else if (isInLava() && (!this.onGround || dMethod015 > dMethod005)) {
                swimUpward(FluidTags.LAVA);
            } else if ((this.onGround || (z && dMethod015 <= dMethod005)) && this.jumpingCooldown == 0) {
                jump();
                this.jumpingCooldown = 10;
            }
        }
        float f = this.input.movementSideways * 0.98f;
        float f2 = this.input.movementForward * 0.98f;
        if (hasStatusEffect(StatusEffects.SLOW_FALLING) || hasStatusEffect(StatusEffects.LEVITATION)) {
            resetFallDistance();
        }
        travel(new Vec3d(f, 0.0f, f2));
    }

    public void travel(Vec3d vec3d) {
        if (this.isSwimming && !this.player.hasVehicle()) {
            double d = getRotationVector().y;
            double d2 = d < -0.2d ? 0.085d : 0.06d;
            BlockPos blockPos = new BlockPos(MathHelper.floor(this.pos.x), MathHelper.floor((this.pos.y + 1.0d) - 0.1d), MathHelper.floor(this.pos.z));
            if (d <= 0.0d || this.input.playerInput.jump() || !this.player.getWorld().getBlockState(blockPos).getFluidState().isEmpty()) {
                this.velocity = this.velocity.add(0.0d, (d - this.velocity.y) * d2, 0.0d);
            }
        }
        double d3 = this.velocity.y;
        double d4 = 0.08d;
        boolean z = this.velocity.y <= 0.0d;
        if (this.velocity.y <= 0.0d && hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            d4 = 0.01d;
            resetFallDistance();
        }
        if (isTouchingWater() && this.player.shouldSwimInFluids()) {
            double d5 = this.pos.y;
            float f = isSprinting() ? 0.9f : 0.8f;
            float fMethod029 = 0.02f;
            float attributeValue = (float) getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
            if (!this.onGround) {
                attributeValue *= 0.5f;
            }
            if (attributeValue > 0.0f) {
                f += ((0.54600006f - f) * attributeValue) / 3.0f;
                fMethod029 = 0.02f + (((getMovementSpeedAttribute() - 0.02f) * attributeValue) / 3.0f);
            }
            if (hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
                f = 0.96f;
            }
            updateVelocity(fMethod029, vec3d);
            move(this.velocity);
            Vec3d vec3d2 = this.velocity;
            if (this.horizontalCollision && isClimbing()) {
                vec3d2 = new Vec3d(vec3d2.x, 0.2d, vec3d2.z);
            }
            this.velocity = vec3d2.multiply(f, 0.8d, f);
            Vec3d vec3dApplyFluidMovingSpeed = this.player.applyFluidMovingSpeed(d4, z, this.velocity);
            this.velocity = vec3dApplyFluidMovingSpeed;
            if (this.horizontalCollision && canMoveTo(vec3dApplyFluidMovingSpeed.x, ((vec3dApplyFluidMovingSpeed.y + 0.6d) - this.pos.y) + d5, vec3dApplyFluidMovingSpeed.z)) {
                this.velocity = new Vec3d(vec3dApplyFluidMovingSpeed.x, 0.3d, vec3dApplyFluidMovingSpeed.z);
            }
        } else if (isInLava() && this.player.shouldSwimInFluids()) {
            double d6 = this.pos.y;
            updateVelocity(0.02f, vec3d);
            move(this.velocity);
            if (getFluidHeight(FluidTags.LAVA) <= getSwimHeight()) {
                this.velocity = this.velocity.multiply(halfBlock, 0.8d, halfBlock);
                this.velocity = this.player.applyFluidMovingSpeed(d4, z, this.velocity);
            } else {
                this.velocity = this.velocity.multiply(halfBlock);
            }
            if (!this.player.hasNoGravity()) {
                this.velocity = this.velocity.add(0.0d, (-d4) / 4.0d, 0.0d);
            }
            if (this.horizontalCollision && canMoveTo(this.velocity.x, ((this.velocity.y + 0.6d) - this.pos.y) + d6, this.velocity.z)) {
                this.velocity = new Vec3d(this.velocity.x, 0.3d, this.velocity.z);
            }
        } else if (this.isFallFlying) {
            if (this.velocity.y > -0.5d) {
                this.fallDistance = 1.0f;
            }
            Vec3d vec3dMethod007 = getRotationVector();
            float f2 = this.pitch * 0.017453292f;
            double dSqrt = Math.sqrt((vec3dMethod007.x * vec3dMethod007.x) + (vec3dMethod007.z * vec3dMethod007.z));
            double dHorizontalLength = this.velocity.horizontalLength();
            double length = vec3dMethod007.length();
            float fCos = MathHelper.cos(f2);
            float fMin = (float) (((double) fCos) * ((double) fCos) * Math.min(1.0d, length / 0.4d));
            Vec3d vec3dAdd = this.velocity.add(0.0d, d4 * ((-1.0d) + (((double) fMin) * 0.75d)), 0.0d);
            if (vec3dAdd.y < 0.0d && dSqrt > 0.0d) {
                double d7 = vec3dAdd.y * (-0.1d) * ((double) fMin);
                vec3dAdd = vec3dAdd.add((vec3dMethod007.x * d7) / dSqrt, d7, (vec3dMethod007.z * d7) / dSqrt);
            }
            if (f2 < 0.0f && dSqrt > 0.0d) {
                double d8 = dHorizontalLength * ((double) (-MathHelper.sin(f2))) * 0.04d;
                vec3dAdd = vec3dAdd.add(((-vec3dMethod007.x) * d8) / dSqrt, d8 * 3.2d, ((-vec3dMethod007.z) * d8) / dSqrt);
            }
            if (dSqrt > 0.0d) {
                vec3dAdd = vec3dAdd.add((((vec3dMethod007.x / dSqrt) * dHorizontalLength) - vec3dAdd.x) * 0.1d, 0.0d, (((vec3dMethod007.z / dSqrt) * dHorizontalLength) - vec3dAdd.z) * 0.1d);
            }
            this.velocity = vec3dAdd.multiply(0.99d, 0.98d, 0.99d);
            move(this.velocity);
        } else {
            BlockPos blockPosMethod006 = getLandingBlockPos();
            float slipperiness = this.player.getWorld().getBlockState(blockPosMethod006).getBlock().getSlipperiness();
            float f3 = this.onGround ? slipperiness * 0.91f : 0.91f;
            Vec3d vec3dMethod014 = applyGroundMovement(vec3d, slipperiness);
            double amplifier = vec3dMethod014.y;
            if (hasStatusEffect(StatusEffects.LEVITATION)) {
                StatusEffectInstance statusEffectInstanceMethod018 = getStatusEffect(StatusEffects.LEVITATION);
                if (statusEffectInstanceMethod018 != null) {
                    amplifier += ((0.05d * ((double) (statusEffectInstanceMethod018.getAmplifier() + 1))) - vec3dMethod014.y) * 0.2d;
                }
            } else if (this.player.getWorld().isClient() && !this.player.getWorld().isChunkLoaded(blockPosMethod006)) {
                amplifier = this.pos.y > ((double) this.player.getWorld().getBottomY()) ? -0.1d : 0.0d;
            } else if (!this.player.hasNoGravity()) {
                amplifier -= d4;
            }
            if (this.player.hasNoDrag()) {
                this.velocity = new Vec3d(vec3dMethod014.x, amplifier, vec3dMethod014.z);
            } else {
                this.velocity = new Vec3d(vec3dMethod014.x * ((double) f3), amplifier * 0.9800000190734863d, vec3dMethod014.z * ((double) f3));
            }
        }
        if (!this.player.getAbilities().flying || this.player.hasVehicle()) {
            return;
        }
        this.velocity = new Vec3d(this.velocity.x, d3 * 0.6d, this.velocity.z);
        resetFallDistance();
    }

    public Vec3d applyGroundMovement(Vec3d vec3d, float f) {
        updateVelocity(computeMovementSpeed(f), vec3d);
        this.velocity = applyClimbingSpeed(this.velocity);
        move(this.velocity);
        Vec3d vec3d2 = this.velocity;
        BlockState state = getState(posToBlockPos(this.pos));
        if ((this.horizontalCollision || this.isJumping) && (isClimbing() || (state != null && state.isOf(Blocks.POWDER_SNOW) && PowderSnowBlock.canWalkOnPowderSnow(this.player)))) {
            vec3d2 = new Vec3d(vec3d2.x, 0.2d, vec3d2.z);
        }
        return vec3d2;
    }

    public void updateVelocity(float f, Vec3d vec3d) {
        this.velocity = this.velocity.add(Entity.movementInputToVelocity(vec3d, f, this.yaw));
    }

    public float computeMovementSpeed(float f) {
        return this.onGround ? getMovementSpeedAttribute() * (0.21600002f / ((f * f) * f)) : getAirSpeed();
    }

    public float getAirSpeed() {
        if (this.input.playerInput.sprint()) {
            return 0.02f + 0.006f;
        }
        return 0.02f;
    }

    public Vec3d getEyePos() {
        return new Vec3d(this.pos.x, getEyeY(), this.pos.z);
    }

    public float getMovementSpeedAttribute() {
        return (float) getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
    }

    public void move(Vec3d vec3d) {
        Vec3d vec3dMethod013 = adjustForCollisions(adjustForLedge(vec3d));
        if (vec3dMethod013.lengthSquared() > 1.0E-7d) {
            this.pos = this.pos.add(vec3dMethod013);
            this.boundingBox = this.player.dimensions.getBoxAt(this.pos);
        }
        boolean z = !MathHelper.approximatelyEquals(vec3d.x, vec3dMethod013.x);
        boolean z2 = !MathHelper.approximatelyEquals(vec3d.z, vec3dMethod013.z);
        this.horizontalCollision = z || z2;
        this.verticalCollision = vec3d.y != vec3dMethod013.y;
        this.onGround = this.verticalCollision && vec3d.y < 0.0d;
        if (!isTouchingWater()) {
            updateTouchingWater();
        }
        if (this.onGround) {
            resetFallDistance();
        } else if (vec3d.y < 0.0d) {
            this.fallDistance -= (float) vec3d.y;
        }
        Vec3d vec3d2 = this.velocity;
        if (this.horizontalCollision || this.verticalCollision) {
            this.velocity = new Vec3d(z ? 0.0d : vec3d2.x, this.onGround ? 0.0d : vec3d2.y, z2 ? 0.0d : vec3d2.z);
        }
    }

    public Vec3d adjustForCollisions(Vec3d vec3d) {
        Box boxOffset = new Box(-0.3d, 0.0d, -0.3d, 0.3d, 1.8d, 0.3d).offset(this.pos);
        List listEmptyList = Collections.emptyList();
        Vec3d vec3dAdjustMovementForCollisions = vec3d.lengthSquared() == 0.0d ? vec3d : Entity.adjustMovementForCollisions(this.player, vec3d, boxOffset, this.player.getWorld(), listEmptyList);
        boolean z = vec3d.x != vec3dAdjustMovementForCollisions.x;
        boolean z2 = vec3d.y != vec3dAdjustMovementForCollisions.y;
        boolean z3 = vec3d.z != vec3dAdjustMovementForCollisions.z;
        boolean z4 = this.onGround || (z2 && vec3d.y < 0.0d);
        if (this.player.getStepHeight() > 0.0f && z4 && (z || z3)) {
            Vec3d vec3dAdjustMovementForCollisions2 = Entity.adjustMovementForCollisions(this.player, new Vec3d(vec3d.x, this.player.getStepHeight(), vec3d.z), boxOffset, this.player.getWorld(), listEmptyList);
            Vec3d vec3dAdjustMovementForCollisions3 = Entity.adjustMovementForCollisions(this.player, new Vec3d(0.0d, this.player.getStepHeight(), 0.0d), boxOffset.stretch(vec3d.x, 0.0d, vec3d.z), this.player.getWorld(), listEmptyList);
            Vec3d vec3dAdd = Entity.adjustMovementForCollisions(this.player, new Vec3d(vec3d.x, 0.0d, vec3d.z), boxOffset.offset(vec3dAdjustMovementForCollisions3), this.player.getWorld(), listEmptyList).add(vec3dAdjustMovementForCollisions3);
            if (vec3dAdjustMovementForCollisions3.y < this.player.getStepHeight() && vec3dAdd.horizontalLengthSquared() > vec3dAdjustMovementForCollisions2.horizontalLengthSquared()) {
                vec3dAdjustMovementForCollisions2 = vec3dAdd;
            }
            if (vec3dAdjustMovementForCollisions2.horizontalLengthSquared() > vec3dAdjustMovementForCollisions.horizontalLengthSquared()) {
                return vec3dAdjustMovementForCollisions2.add(Entity.adjustMovementForCollisions(this.player, new Vec3d(0.0d, (-vec3dAdjustMovementForCollisions2.y) + vec3d.y, 0.0d), boxOffset.offset(vec3dAdjustMovementForCollisions2), this.player.getWorld(), listEmptyList));
            }
        }
        return vec3dAdjustMovementForCollisions;
    }

    public void resetFallDistance() {
        this.fallDistance = 0.0f;
    }

    public void jump() {
        this.velocity = this.velocity.add(0.0d, ((double) getJumpVelocity()) - this.velocity.y, 0.0d);
        if (isSprinting()) {
            float radians = (float) Math.toRadians(this.yaw);
            this.velocity = this.velocity.add(((double) (-MathHelper.sin(radians))) * 0.2d, 0.0d, ((double) MathHelper.cos(radians)) * 0.2d);
        }
    }

    public Vec3d applyClimbingSpeed(Vec3d vec3d) {
        if (!isClimbing()) {
            return vec3d;
        }
        resetFallDistance();
        double dClamp = MathHelper.clamp(vec3d.x, -0.15000000596046448d, 0.15000000596046448d);
        double dClamp2 = MathHelper.clamp(vec3d.z, -0.15000000596046448d, 0.15000000596046448d);
        double dMax = Math.max(vec3d.y, -0.15000000596046448d);
        if (dMax < 0.0d && !getState(posToBlockPos(this.pos)).isOf(Blocks.SCAFFOLDING) && this.player.isHoldingOntoLadder()) {
            dMax = 0.0d;
        }
        return new Vec3d(dClamp, dMax, dClamp2);
    }

    public boolean isClimbing() {
        BlockPos blockPosPosToBlockPos = posToBlockPos(this.pos);
        BlockState state = getState(blockPosPosToBlockPos);
        if (state.isIn(BlockTags.CLIMBABLE)) {
            return true;
        }
        return (state.getBlock() instanceof TrapdoorBlock) && isTrapdoorLadder(blockPosPosToBlockPos, state);
    }

    public boolean isTrapdoorLadder(BlockPos blockPos, BlockState blockState) {
        if (!((Boolean) blockState.get(TrapdoorBlock.OPEN)).booleanValue()) {
            return false;
        }
        BlockState blockState2 = this.player.getWorld().getBlockState(blockPos.down());
        return blockState2.isOf(Blocks.LADDER) && blockState2.get(LadderBlock.FACING).equals(blockState.get(TrapdoorBlock.FACING));
    }

    public Vec3d adjustForLedge(Vec3d vec3d) {
        double d;
        if (vec3d.y <= 0.0d && isNearGround()) {
            double d2 = vec3d.x;
            double d3 = vec3d.z;
            while (d2 != 0.0d && this.player.getWorld().isSpaceEmpty(this.player, this.boundingBox.offset(d2, -0.5d, 0.0d))) {
                if (d2 < 0.05d && d2 >= (-0.05d)) {
                    d2 = 0.0d;
                    break;
                }
                d2 += d2 > 0.0d ? -0.05d : 0.05d;
            }
            while (d3 != 0.0d && this.player.getWorld().isSpaceEmpty(this.player, this.boundingBox.offset(0.0d, -0.5d, d3))) {
                if (d3 < 0.05d && d3 >= (-0.05d)) {
                    d3 = 0.0d;
                    break;
                }
                d3 += d3 > 0.0d ? -0.05d : 0.05d;
            }
            while (d2 != 0.0d && d3 != 0.0d && this.player.getWorld().isSpaceEmpty(this.player, this.boundingBox.offset(d2, -0.5d, d3))) {
                if (d2 >= 0.05d || d2 < (-0.05d)) {
                    d = d2 > 0.0d ? d2 - 0.05d : d2 + 0.05d;
                } else {
                    d = 0.0d;
                }
                d2 = d;
                if (d3 < 0.05d && d3 >= (-0.05d)) {
                    d3 = 0.0d;
                    break;
                }
                d3 += d3 > 0.0d ? -0.05d : 0.05d;
            }
            if (vec3d.x != d2 || vec3d.z != d3) {
                this.ledgeClipped = true;
            }
            if (shouldClipAtLedge()) {
                vec3d = new Vec3d(d2, vec3d.y, d3);
            }
        }
        return vec3d;
    }

    public boolean shouldClipAtLedge() {
        return this.input.playerInput.sneak() || this.input.forceSafeWalk;
    }

    public boolean isNearGround() {
        return this.onGround || (((double) this.fallDistance) < halfBlock && !this.player.getWorld().isSpaceEmpty(this.player, this.boundingBox.offset(0.0d, ((double) this.fallDistance) - halfBlock, 0.0d)));
    }

    public boolean isSprinting() {
        return this.sprinting;
    }

    public float getJumpVelocity() {
        return (0.42f * getJumpVelocityMultiplier()) + getJumpBoostVelocity();
    }

    public float getJumpBoostVelocity() {
        if (hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            return 0.1f * (getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1);
        }
        return 0.0f;
    }

    public float getJumpVelocityMultiplier() {
        float jumpVelocityMultiplier = 0.0f;
        Block block = getState(posToBlockPos(this.pos)).getBlock();
        if (block != null) {
            jumpVelocityMultiplier = block.getJumpVelocityMultiplier();
        }
        float jumpVelocityMultiplier2 = 0.0f;
        Block block2 = getState(getLandingBlockPos()).getBlock();
        if (block2 != null) {
            jumpVelocityMultiplier2 = block2.getJumpVelocityMultiplier();
        }
        return jumpVelocityMultiplier == 1.0f ? jumpVelocityMultiplier2 : jumpVelocityMultiplier;
    }

    public boolean canMoveTo(double d, double d2, double d3) {
        return isBoxFree(this.boundingBox.offset(d, d2, d3));
    }

    public boolean isBoxFree(Box box) {
        return this.player.getWorld().isSpaceEmpty(this.player, box) && !this.player.getWorld().containsFluid(box);
    }

    public void swimUpward(TagKey<Fluid> tagKey) {
        this.velocity = this.velocity.add(0.0d, 0.03999999910593033d, 0.0d);
    }

    public BlockPos getLandingBlockPos() {
        return BlockPos.ofFloored(this.pos.x, this.boundingBox.minY - 0.5000001d, this.pos.z);
    }

    public double getSwimHeight() {
        return ((double) this.player.getStandingEyeHeight()) < 0.4d ? 0.0d : 0.4d;
    }

    public boolean isTouchingWater() {
        return this.touchingWater;
    }

    public boolean isInLava() {
        return this.fluidHeight.getDouble(FluidTags.LAVA) > 0.0d;
    }

    public void updateTouchingWater() {
        if ((this.player.getVehicle() instanceof BoatEntity) && !this.player.getVehicle().isSubmergedInWater()) {
            this.touchingWater = false;
        } else if (!updateMovementInFluid(FluidTags.WATER, 0.014d)) {
            this.touchingWater = false;
        } else {
            resetFallDistance();
            this.touchingWater = true;
        }
    }

    public void updateSwimming() {
        if (this.isSwimming) {
            this.isSwimming = isSprinting() && isTouchingWater() && !this.player.hasVehicle();
        } else {
            this.isSwimming = isSprinting() && isSubmergedInWater() && !this.player.hasVehicle() && this.player.getWorld().getFluidState(posToBlockPos(this.pos)).isIn(FluidTags.WATER);
        }
    }

    public void updateSubmergedFluids() {
        this.submergedInWater = this.submergedFluidTags.contains(FluidTags.WATER);
        this.submergedFluidTags.clear();
        double dMethod020 = getEyeY() - 0.1111111119389534d;
        if ((this.player.getVehicle()) instanceof BoatEntity vehicle ) {
            BoatEntity boatEntity = vehicle;
            if (!boatEntity.isSubmergedInWater() && boatEntity.getBoundingBox().maxY >= dMethod020 && boatEntity.getBoundingBox().minY <= dMethod020) {
                return;
            }
        }
        BlockPos blockPosOfFloored = BlockPos.ofFloored(this.pos.x, dMethod020, this.pos.z);
        FluidState fluidState = this.player.getWorld().getFluidState(blockPosOfFloored);
        if (blockPosOfFloored.getY() + fluidState.getHeight(this.player.getWorld(), blockPosOfFloored) > dMethod020) {
            this.submergedFluidTags.addAll(fluidState.streamTags().toList());
        }
    }

    public double getEyeY() {
        return this.pos.y + ((double) this.player.getStandingEyeHeight());
    }

    public boolean isSubmergedInWater() {
        return this.submergedInWater && isTouchingWater();
    }

    public double getFluidHeight(TagKey<Fluid> tagKey) {
        return this.fluidHeight.getDouble(tagKey);
    }

    public boolean updateMovementInFluid(TagKey<Fluid> tagKey, double d) {
        if (isRegionUnloaded()) {
            return false;
        }
        Box boxContract = this.boundingBox.contract(0.001d);
        int iFloor = MathHelper.floor(boxContract.minX);
        int iCeil = MathHelper.ceil(boxContract.maxX);
        int iFloor2 = MathHelper.floor(boxContract.minY);
        int iCeil2 = MathHelper.ceil(boxContract.maxY);
        int iFloor3 = MathHelper.floor(boxContract.minZ);
        int iCeil3 = MathHelper.ceil(boxContract.maxZ);
        double dMax = 0.0d;
        boolean z = false;
        Vec3d vec3dMultiply = Vec3d.ZERO;
        int i = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int i2 = iFloor; i2 < iCeil; i2++) {
            for (int i3 = iFloor2; i3 < iCeil2; i3++) {
                for (int i4 = iFloor3; i4 < iCeil3; i4++) {
                    mutable.set(i2, i3, i4);
                    FluidState fluidState = this.player.getWorld().getFluidState(mutable);
                    if (fluidState.isIn(tagKey)) {
                        double height = i3 + fluidState.getHeight(this.player.getWorld(), mutable);
                        if (height >= boxContract.minY) {
                            z = true;
                            dMax = Math.max(height - boxContract.minY, dMax);
                            if (1 != 0) {
                                Vec3d velocity = fluidState.getVelocity(this.player.getWorld(), mutable);
                                if (dMax < 0.4d) {
                                    velocity = velocity.multiply(dMax);
                                }
                                vec3dMultiply = vec3dMultiply.add(velocity);
                                i++;
                            }
                        }
                    }
                }
            }
        }
        if (vec3dMultiply.length() > 0.0d) {
            if (i > 0) {
                vec3dMultiply = vec3dMultiply.multiply(1.0d / ((double) i));
            }
            Vec3d vec3dMultiply2 = vec3dMultiply.multiply(d);
            if (Math.abs(this.velocity.x) < 0.003d && Math.abs(this.velocity.z) < 0.003d && vec3dMultiply2.length() < 0.0045d) {
                vec3dMultiply2 = vec3dMultiply2.normalize().multiply(0.0045d);
            }
            this.velocity = this.velocity.add(vec3dMultiply2);
        }
        this.fluidHeight.put(tagKey, dMax);
        return z;
    }

    public boolean isRegionUnloaded() {
        Box boxExpand = this.boundingBox.expand(1.0d);
        return !this.player.getWorld().isRegionLoaded(MathHelper.floor(boxExpand.minX), MathHelper.floor(boxExpand.minZ), MathHelper.ceil(boxExpand.maxX), MathHelper.ceil(boxExpand.maxZ));
    }

    public Vec3d getRotationVector() {
        return computeRotationVector(this.pitch, this.yaw);
    }

    public Vec3d computeRotationVector(float f, float f2) {
        float f3 = (float) ((((double) f) * 3.141592653589793d) / 180.0d);
        float f4 = (float) ((((double) (-f2)) * 3.141592653589793d) / 180.0d);
        float fCos = MathHelper.cos(f4);
        float fSin = MathHelper.sin(f4);
        float fCos2 = MathHelper.cos(f3);
        return new Vec3d(fSin * fCos2, -MathHelper.sin(f3), fCos * fCos2);
    }

    public boolean hasStatusEffect(RegistryEntry<StatusEffect> registryEntry) {
        StatusEffectInstance statusEffect = this.player.getStatusEffect(registryEntry);
        return statusEffect != null && statusEffect.getDuration() >= this.tickCount;
    }

    public StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> registryEntry) {
        StatusEffectInstance statusEffect = this.player.getStatusEffect(registryEntry);
        if (statusEffect == null || statusEffect.getDuration() < this.tickCount) {
            return null;
        }
        return statusEffect;
    }

    public double getAttributeValue(RegistryEntry<EntityAttribute> registryEntry) {
        return this.player.getAttributes().getValue(registryEntry);
    }

    public SimulatedPlayer m22clone() {
        return new SimulatedPlayer(this.player, this.input, this.pos, this.velocity, this.boundingBox, this.yaw, this.pitch, this.sprinting, this.fallDistance, this.jumpingCooldown, this.isJumping, this.isFallFlying, this.onGround, this.horizontalCollision, this.verticalCollision, this.touchingWater, this.isSwimming, this.submergedInWater, new Object2DoubleArrayMap(this.fluidHeight), new HashSet(this.submergedFluidTags));
    }

    public BlockPos posToBlockPos(Vec3d vec3d) {
        return new BlockPos(MathHelper.floor(vec3d.x), MathHelper.floor(vec3d.y), MathHelper.floor(vec3d.z));
    }

    public BlockState getState(BlockPos blockPos) {
        return this.player.getWorld().getBlockState(blockPos);
    }
}
