package aethereal.module;

import aethereal.Lang;
import aethereal.annotation.Aliases;
import aethereal.event.KeyInputEvent;
import aethereal.event.PlayerTravelEvent;
import aethereal.type.KeyPressState;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.KeybindSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.util.RotationManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Aliases(aliases = {"Water Speed", "Matrix Water Speed", "WaterSpeed"})
public class WaterSpeedModule extends Module {
    private static final float S20 = 0.7015F;
    private static final float S0 = 0.595F;
    private static final float S15 = 0.6499F;
    private static final float S25 = 0.749F;

    public final ModeSetting<WaterSpeedMode> mode;
    public final KeybindSetting boostkey;
    private final Mc mc;
    private long boostEndTime = 0;
    private boolean isBoosting = false;
    private boolean boostPressed = false;

    public WaterSpeedModule() {
        super(ModuleTab.MOVEMENT, "WaterSpeed");
        this.mc = Mc.INSTANCE;
        this.mode = new ModeSetting<WaterSpeedMode>(Lang.WATERSPEED_MODE).values(WaterSpeedMode.class);
        this.boostkey = new KeybindSetting(Lang.WATERSPEED_BOOSTKEY).visible(() -> this.mode.isSelected(WaterSpeedMode.METAHVH));
        addSettings(this.mode, this.boostkey);

        register(KeyInputEvent.class, e -> {
            if (!isState() || this.mc.getPlayer() == null || this.mc.getWorld() == null) return;
            if (e.key() == this.boostkey.getKey() && e.action() == KeyPressState.PRESS) {
                this.boostPressed = true;
            }
        });

        register(PlayerTravelEvent.class, e -> {
            if (!isState()) return;
            ClientPlayerEntity player = this.mc.getPlayer();
            if (player == null || this.mc.getWorld() == null) return;

            if (this.boostPressed) {
                this.isBoosting = true;
                this.boostEndTime = System.currentTimeMillis() + 900;
                this.boostPressed = false;
            }

            if (this.isBoosting && System.currentTimeMillis() > this.boostEndTime) {
                this.isBoosting = false;
            }

            if (this.mode.isSelected(WaterSpeedMode.GRIM)) {
                if (this.mc.getGameOptions().jumpKey.isPressed()) {
                    if (player.isTouchingWater()) {
                        double waterLevel;
                        BlockPos playerPos = BlockPos.ofFloored(player.getX(), player.getY(), player.getZ());
                        try {
                            waterLevel = this.mc.getWorld().getFluidState(playerPos).getHeight(this.mc.getWorld(), playerPos);
                        } catch (NoSuchMethodError noSuchMethodError) {
                            BlockState blockState = this.mc.getWorld().getBlockState(playerPos);
                            if (blockState.isOf(Blocks.WATER)) {
                                waterLevel = playerPos.getX() + 1.0;
                            } else {
                                waterLevel = playerPos.getY();
                            }
                        }

                        double playerEyeY = player.getY() + player.getEyeHeight(player.getPose());
                        if (playerEyeY >= waterLevel - 0.2 && playerEyeY <= waterLevel + 0.2) {
                            player.setVelocity(player.getVelocity().x, 0.2, player.getVelocity().z);
                            setMotion(getMotion() * 2.0);
                        }
                    } else if (player.isTouchingWater()) {
                        player.setVelocity(player.getVelocity().x, Math.max(player.getVelocity().y, 0.03), player.getVelocity().z);
                    }
                }
            }

            if (this.mode.isSelected(WaterSpeedMode.MATRIX)) {
                if (player.verticalCollision || player.horizontalCollision) {
                    return;
                }

                if (player.isSwimming() && player.isTouchingWater()) {
                    float speed = 0.5f / 10F;
                    if (this.mc.getGameOptions().jumpKey.isPressed()) {
                        player.setVelocity(player.getVelocity().x, player.getVelocity().y + 0.05, player.getVelocity().z);
                    }
                    if (this.mc.getGameOptions().sneakKey.isPressed()) {
                        player.setVelocity(player.getVelocity().x, player.getVelocity().y - 0.05, player.getVelocity().z);
                    }
                    setMotion(getMotion());
                    e.speed += speed;
                }
            }

            if (this.mode.isSelected(WaterSpeedMode.METAHVH)) {
                if (player.verticalCollision || player.horizontalCollision) {
                    return;
                }

                if ((player.isTouchingWater() || player.isInLava()) && player.isSwimming()) {
                    ItemStack offHandItem = player.getOffHandStack();
                    StatusEffectInstance speedEffect = player.getStatusEffect(StatusEffects.SPEED);
                    StatusEffectInstance deEffect = player.getStatusEffect(StatusEffects.SLOWNESS);
                    String itemName = offHandItem.getName().getString();
                    float appliedSpeed = 0;

                    if (speedEffect != null) {
                        if (speedEffect.getAmplifier() == 2) {
                            if (itemName.contains("Шар Геракла 2") || itemName.contains("Шар CHAMPION") || itemName.contains("Шар Аида 2") || itemName.contains("Шар GOD") || itemName.contains("КУБИК-РУБИК") || itemName.contains("Шар BUNNY")) {
                                appliedSpeed = S20 * 1.14F;
                            } else if (itemName.contains("Талисман Венома")) {
                                appliedSpeed = S25 * 1.14F;
                            } else if (itemName.contains("Талисман Картеля")) {
                                appliedSpeed = S15 * 1.14F;
                            } else {
                                appliedSpeed = S0 * 1.14F;
                            }
                        } else if (speedEffect.getAmplifier() == 1) {
                            if (itemName.contains("Шар Геракла 2") || itemName.contains("Шар CHAMPION") || itemName.contains("Шар Аида 2") || itemName.contains("Шар GOD") || itemName.contains("КУБИК-РУБИК") || itemName.contains("Шар BUNNY")) {
                                appliedSpeed = S20;
                            } else if (itemName.contains("Талисман Венома")) {
                                appliedSpeed = S25;
                            } else if (itemName.contains("Талисман Картеля")) {
                                appliedSpeed = S15;
                            } else {
                                appliedSpeed = S0;
                            }
                        }
                    } else {
                        if (itemName.contains("Шар Геракла 2") || itemName.contains("Шар CHAMPION") || itemName.contains("Шар Аида 2") || itemName.contains("Шар GOD") || itemName.contains("КУБИК-РУБИК") || itemName.contains("Шар BUNNY")) {
                            appliedSpeed = S20 * 0.68F;
                        } else if (itemName.contains("Талисман Венома")) {
                            appliedSpeed = S25 * 0.68F;
                        } else if (itemName.contains("Талисман Картеля")) {
                            appliedSpeed = S15 * 0.68F;
                        } else {
                            appliedSpeed = S0 * 0.68F;
                        }
                    }

                    if (deEffect != null) {
                        appliedSpeed *= 0.85f;
                    }

                    if (this.isBoosting) {
                        appliedSpeed *= 1.75F;
                    }

                    setSpeed(appliedSpeed);
                }
            }
        });
    }

    private float getMotion() {
        Vec3d vec3d = this.mc.getPlayer().getVelocity();
        return (float) Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
    }

    private void setMotion(double d) {
        ClientPlayerEntity player = this.mc.getPlayer();
        double forward = player.input.movementForward;
        double strafe = player.input.movementSideways;
        float yaw = RotationManager.INSTANCE.getMoveRotation().getYaw();

        if (forward == 0.0 && strafe == 0.0) {
            player.setVelocity(0.0, player.getVelocity().y, 0.0);
        } else {
            if (forward != 0.0) {
                if (strafe > 0.0) {
                    yaw += (float) (forward > 0.0 ? -45 : 45);
                } else if (strafe < 0.0) {
                    yaw += (float) (forward > 0.0 ? 45 : -45);
                }
                strafe = 0.0;
                if (forward > 0.0) {
                    forward = 1.0;
                } else if (forward < 0.0) {
                    forward = -1.0;
                }
            }
            player.setVelocity(
                    forward * d * Math.cos(Math.toRadians(yaw + 90.0f)) + strafe * d * Math.sin(Math.toRadians(yaw + 90.0f)),
                    player.getVelocity().y,
                    forward * d * Math.sin(Math.toRadians(yaw + 90.0f)) - strafe * d * Math.cos(Math.toRadians(yaw + 90.0f))
            );
        }
    }

    private void setSpeed(float f) {
        ClientPlayerEntity player = this.mc.getPlayer();
        float yaw = RotationManager.INSTANCE.getMoveRotation().getYaw();
        float forward = player.input.movementForward;
        float strafe = player.input.movementSideways;
        if (forward != 0.0f) {
            if (strafe > 0.0f) {
                yaw += (float) (forward > 0.0f ? -45 : 45);
            } else if (strafe < 0.0f) {
                yaw += (float) (forward > 0.0f ? 45 : -45);
            }
            strafe = 0.0f;
            if (forward > 0.0f) {
                forward = 1.0f;
            } else if (forward < 0.0f) {
                forward = -1.0f;
            }
        }
        player.setVelocity(
                forward * f * Math.cos(Math.toRadians(yaw + 90.0)) + strafe * f * Math.sin(Math.toRadians(yaw + 90.0)),
                player.getVelocity().y,
                forward * f * Math.sin(Math.toRadians(yaw + 90.0)) - strafe * f * Math.cos(Math.toRadians(yaw + 90.0))
        );
    }
}
