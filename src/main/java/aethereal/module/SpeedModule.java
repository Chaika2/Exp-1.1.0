package aethereal.module;
import aethereal.math.DirectionalInput;
import aethereal.type.EventPhase;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.util.IteratorUtil;
import aethereal.Lang;
import aethereal.util.MathUtil;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.event.MovementInputEvent;
import aethereal.util.MovementInputHelper;
import aethereal.ui.setting.NumberSetting;
import aethereal.util.PlayerSnapshotManager;
import aethereal.event.PlayerTickEvent;
import aethereal.event.RotationUpdateEvent;
import aethereal.type.SettingUnit;
import aethereal.render.ShapeRenderer;
import aethereal.math.SimulatedPlayer;
import aethereal.type.SpeedMode;
import aethereal.event.WorldRenderEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class SpeedModule extends Module {
    public final ModeSetting<SpeedMode> modeSetting;
    public final Mc mc;
    public final NumberSetting speedSetting;
    public Entity target;

    public SpeedModule() {
        super(ModuleTab.MOVEMENT, "Speed");
        this.modeSetting = new ModeSetting(Lang.MODE).values(SpeedMode.class);
        this.mc = Mc.INSTANCE;
        this.speedSetting = new NumberSetting(Lang.SPEED_SPEED).currentValue(0.35f).range(0.0f, 0.8f).unit(SettingUnit.BLOCKS).visible(() -> {
            return Boolean.valueOf(this.modeSetting.isSelected(SpeedMode.HOLYWORLD));
        }).step(0.05f);
        addSettings(this.modeSetting, this.speedSetting);
        register(RotationUpdateEvent.class, class345Var -> {
            if (isState() && this.mc.isWorldLoaded() && this.modeSetting.isSelected(SpeedMode.HOLYWORLD) && Objects.requireNonNull(class345Var.stage()) == EventPhase.PRE) {
                this.target = null;
                getNearbyEntities(1.5d).filter(livingEntity -> {
                    return livingEntity.isPlayer() && !FriendManager.isFriend(livingEntity.getName().getString());
                }).min(Comparator.comparingInt(livingEntity2 -> {
                    return livingEntity2.equals(((AttackAuraModule) Expensive.INSTANCE.moduleRepository().get(AttackAuraModule.class)).lastTarget()) ? 0 : 1;
                })).ifPresent(livingEntity3 -> {
                    this.target = livingEntity3;
                });
            }
        });
        register(MovementInputEvent.class, class040Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                switch (((SpeedMode) this.modeSetting.currentValue()).ordinal()) {
                    case 0:
                        if (this.mc.getPlayer().isOnGround()) {
                            class040Var.setJumping(true);
                        }
                        break;
                    case 2:
                        ClientPlayerEntity player = this.mc.getPlayer();
                        if (player != null && this.target != null) {
                            Vec3d vec3dSubtract = this.target.getPos().subtract(player.getPos());
                            if (vec3dSubtract.lengthSquared() > 1.0E-4d) {
                                class040Var.setInput(MovementInputHelper.getDirectionalInputForDegrees(DirectionalInput.NONE, MovementInputHelper.getDegreesRelativeToView(vec3dSubtract, player.getYaw())));
                                break;
                            }
                        }
                        break;
                }
            }
        }, EventPriority.HIGH);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                ClientWorld world = this.mc.getWorld();
                switch (((SpeedMode) this.modeSetting.currentValue()).ordinal()) {
                    case 0:
                        DirectionalInput class041VarFromPlayerInput = DirectionalInput.fromPlayerInput(player);
                        SimulatedPlayer class136VarSimulateLocalPlayer = SimulatedPlayer.simulateLocalPlayer(1);
                        if (class041VarFromPlayerInput.isMoving() && !player.isOnGround() && class136VarSimulateLocalPlayer.onGround && player.fallDistance > 0.5f) {
                            player.setVelocity(player.getVelocity().multiply(2.0d, 1.0d, 2.0d));
                            break;
                        }
                        break;
                    case 1:
                        List list = world.getNonSpectatingEntities(LivingEntity.class, player.getBoundingBox().expand(0.10000000149011612d)).stream().filter(livingEntity -> {
                            return (livingEntity == player || (livingEntity instanceof ArmorStandEntity)) ? false : true;
                        }).toList();
                        if (!list.isEmpty()) {
                            MovementInputHelper.setSpeed(0.1f * list.size());
                        }
                        break;
                    case 2:
                        getNearbyEntities(0.2d).findFirst().ifPresent(livingEntity2 -> {
                            boolean zAnyMatch = BlockPos.stream(SimulatedPlayer.simulateLocalPlayer(1).boundingBox.expand(0.3d, -0.001d, 0.3d)).anyMatch(blockPos -> {
                                return !this.mc.getWorld().getBlockState(blockPos).getCollisionShape(this.mc.getWorld(), blockPos).isEmpty();
                            });
                            double[] dArrDirection = MovementInputHelper.direction(this.speedSetting.currentValue() / 10.0f);
                            if (zAnyMatch) {
                                return;
                            }
                            this.mc.getPlayer().addVelocity(dArrDirection[0], 0.0d, dArrDirection[1]);
                        });
                        break;
                }
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && this.modeSetting.isSelected(SpeedMode.HOLYWORLD) && this.target != null) {
                Matrix4f positionMatrix = class016Var.matrixStack().peek().getPositionMatrix();
                Box boxOffset = this.target.getBoundingBox().offset(MathUtil.interpolate(this.target).subtract(this.target.getPos()));
                ShapeRenderer.INSTANCE.addBox(positionMatrix, boxOffset, 822035524);
                ShapeRenderer.INSTANCE.addOutline(positionMatrix, boxOffset, -922794940, 1.5f);
            }
        });
    }

    public Stream<LivingEntity> getNearbyEntities(double d) {
        PlayerSnapshotManager class369Var = PlayerSnapshotManager.INSTANCE;
        ArrayList<Box> arrayList = new ArrayList<Box>();
        arrayList.add(SimulatedPlayer.simulateLocalPlayer(1).boundingBox.expand(d));
        class369Var.getSnapshots(this.mc.getPlayer(), 2).forEach(class373Var -> {
            arrayList.add(class373Var.box.expand(d));
        });
        Stream<Entity> stream = IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream();
        Class<LivingEntity> cls = LivingEntity.class;
        Objects.requireNonNull(LivingEntity.class);
        Stream<Entity> streamFilter = stream.filter((v1) -> {
            return cls.isInstance(v1);
        });
        Class<LivingEntity> cls2 = LivingEntity.class;
        Objects.requireNonNull(LivingEntity.class);
        return streamFilter.map(livingEntityObj -> {
            return (LivingEntity) cls2.cast(livingEntityObj);
        }).filter(livingEntity -> {
            return !(livingEntity instanceof ArmorStandEntity) && livingEntity != this.mc.getPlayer() && livingEntity.getEyePos().distanceTo(this.mc.getPlayer().getEyePos()) <= 8.0d && class369Var.getSnapshots(livingEntity, 3).anyMatch(class373Var2 -> {
                return arrayList.stream().anyMatch(box -> {
                    return box.intersects(class373Var2.box);
                });
            });
        });
    }
}
