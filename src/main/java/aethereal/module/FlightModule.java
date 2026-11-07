package aethereal.module;
import aethereal.event.BlockUpdateEvent;
import aethereal.type.BlockUpdateType;
import aethereal.Expensive;
import aethereal.type.FlightMode;
import aethereal.util.HitPointResolver;
import aethereal.util.IteratorUtil;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.math.DirectionalInput;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.util.MovementInputHelper;
import aethereal.type.NotificationType;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PacketSendEvent;
import aethereal.net.PacketSender;
import aethereal.util.PlayerActionUtil;
import aethereal.event.PlayerInitEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.util.RotationManager;
import aethereal.math.RotationMath;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class FlightModule extends Module {
    final ModeSetting<FlightMode> modeSetting;
    final NumberSetting horizontalSpeed;
    final NumberSetting verticalSpeed;
    final NumberSetting acceleration;
    BlockPos placedBlockPos;
    final Mc mc;
    final HitPointResolver hitPointResolver;
    boolean grantedFlight;
    double smoothedHorizontalSpeed;
    double lastDirectionX;
    double lastDirectionZ;

    public FlightModule() {
        super(ModuleTab.MOVEMENT, "Flight");
        this.modeSetting = new ModeSetting(Lang.MODE).values(FlightMode.class);
        this.horizontalSpeed = new NumberSetting(Lang.FLIGHT_VALUE_HORIZONTAL_SPEED).currentValue(1.0f).range(0.1f, 10.0f).step(0.1f).unit(SettingUnit.BLOCKS).visible(() -> {
            return Boolean.valueOf(this.modeSetting.isSelected(FlightMode.VANILLA));
        });
        this.verticalSpeed = new NumberSetting(Lang.FLIGHT_VALUE_VERTICAL_SPEED).currentValue(1.0f).range(0.1f, 10.0f).step(0.1f).unit(SettingUnit.BLOCKS).visible(() -> {
            return Boolean.valueOf(this.modeSetting.isSelected(FlightMode.VANILLA));
        });
        this.acceleration = new NumberSetting(Lang.FLIGHT_VALUE_ACCELERATION).currentValue(0.15f).range(0.01f, 1.0f).step(0.01f).unit(SettingUnit.BLOCKS).visible(() -> {
            return Boolean.valueOf(this.modeSetting.isSelected(FlightMode.VANILLA));
        });
        this.placedBlockPos = BlockPos.ORIGIN;
        this.mc = Mc.INSTANCE;
        this.hitPointResolver = new HitPointResolver();
        addSettings(this.modeSetting, this.horizontalSpeed, this.verticalSpeed, this.acceleration);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && this.mc.isWorldLoaded() && class130Var.isPre()) {
                ClientPlayerEntity player = this.mc.getPlayer();
                GameOptions gameOptions = this.mc.getGameOptions();
                switch (((FlightMode) this.modeSetting.currentValue()).ordinal()) {
                    case 0:
                        double vertical = gameOptions.sneakKey.isPressed() ? ((double) (-this.verticalSpeed.currentValue())) / 1.5d : gameOptions.jumpKey.isPressed() ? ((double) this.verticalSpeed.currentValue()) / 1.5d : 0.0d;
                        DirectionalInput input = DirectionalInput.fromPlayerInput(player);
                        if (input.isMoving()) {
                            double radians = Math.toRadians(MovementInputHelper.getMovementDirectionOfInput(player.getYaw(), input));
                            this.lastDirectionX = -Math.sin(radians);
                            this.lastDirectionZ = Math.cos(radians);
                            this.smoothedHorizontalSpeed = approach(this.smoothedHorizontalSpeed, (double) this.horizontalSpeed.currentValue(), (double) this.acceleration.currentValue());
                        } else {
                            this.smoothedHorizontalSpeed = approach(this.smoothedHorizontalSpeed, 0.0d, (double) this.acceleration.currentValue());
                        }
                        player.setVelocity(this.lastDirectionX * this.smoothedHorizontalSpeed, vertical, this.lastDirectionZ * this.smoothedHorizontalSpeed);
                        break;
                    case 1:
                        if (!player.isCreative() && !player.getAbilities().allowFlying) {
                            player.getAbilities().allowFlying = true;
                            this.grantedFlight = true;
                        }
                        break;
                    case 2:
                        onTick();
                        break;
                }
            }
        });
        register(PacketSendEvent.class, class037Var -> {
            if (this.mc.isWorldLoaded() && isState() && this.modeSetting.isSelected(FlightMode.GRIM)) {
                BlinkModule class484Var = (BlinkModule) Expensive.INSTANCE.moduleRepository().get(BlinkModule.class);
                if (class484Var.isState()) {
                    return;
                }
                class484Var.sendPacket(class037Var);
            }
        });
        register(PlayerInitEvent.class, class125Var -> {
            if (this.mc.isWorldLoaded() && isState() && this.modeSetting.isSelected(FlightMode.GRIM)) {
                resetSlot(this.mc.getPlayer().getInventory().selectedSlot);
            }
        });
        register(BlockUpdateEvent.class, class189Var -> {
            if (this.mc.isWorldLoaded() && isState() && this.modeSetting.isSelected(FlightMode.GRIM) && !class189Var.type().equals(BlockUpdateType.LOAD)) {
                class189Var.list().forEach(class190Var -> {
                    BlockPos immutable = class190Var.pos().toImmutable();
                    if (class190Var.state().isAir() && immutable.equals(this.placedBlockPos)) {
                        this.placedBlockPos = BlockPos.ORIGIN;
                    }
                });
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (this.mc.isWorldLoaded() && isState() && this.modeSetting.isSelected(FlightMode.GRIM)) {
                BlinkModule class484Var = (BlinkModule) Expensive.INSTANCE.moduleRepository().get(BlinkModule.class);
                if (class484Var.isState()) {
                    return;
                }
                class484Var.receivePacket(class051Var);
            }
        });
    }

    @Override
    public void deactivate() {
        Mc class815Var = Mc.INSTANCE;
        this.smoothedHorizontalSpeed = 0.0d;
        this.lastDirectionX = 0.0d;
        this.lastDirectionZ = 0.0d;
        if (class815Var.isWorldLoaded()) {
            ClientPlayerEntity player = class815Var.getPlayer();
            if (this.grantedFlight) {
                player.getAbilities().allowFlying = false;
                player.getAbilities().flying = false;
                this.grantedFlight = false;
            }
            resetSlot(player.getInventory().selectedSlot);
        }
        super.deactivate();
    }

    public void onTick() {
        BlockPos immutable = this.mc.getPlayer().getBlockPos().down().toImmutable();
        if (((BlinkModule) Expensive.INSTANCE.moduleRepository().get(BlinkModule.class)).stopWatch.hasElapsed(0L) && this.mc.getWorld().getBlockState(immutable).getOutlineShape(this.mc.getWorld(), immutable).isEmpty() && isBoxClear(new Box(immutable))) {
            ServerUtil.valid1_17().ifPresentOrElse(num -> {
                findBlockSlot().ifPresent(i -> {
                    List<BlockHitResult> listMethod013 = findPlacementPath(this.mc.getPlayer().getEyePos(), immutable);
                    if (listMethod013.isEmpty()) {
                        resetSlot(this.mc.getPlayer().getInventory().selectedSlot);
                        return;
                    }
                    resetSlot(i);
                    for (int j = 0; j < Math.min(1, listMethod013.size()); j++) {
                        placeBlock(this.mc.getPlayer().getInventory().getStack(j), listMethod013.get(j));
                    }
                    PlayerActionUtil.INSTANCE.packetRotate(RotationManager.INSTANCE.getCurrentRotation(), 0.2f, 10.0f);
                });
            }, () -> {
                Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, Text.of("[Flight] Нужна версия " + String.valueOf(Formatting.RED) + "1.17-" + (ServerUtil.isConnectedToServer("holyworld") ? "1.18.2" : "1.20.6")), 3L, TimeUnit.SECONDS);
                switchState();
            });
        } else if (this.placedBlockPos.equals(BlockPos.ORIGIN)) {
            resetSlot(this.mc.getPlayer().getInventory().selectedSlot);
        }
    }

    public OptionalInt findBlockSlot() {
        return PlayerActionUtil.INSTANCE.hotbarSlotsStream(i -> {
            return (this.mc.getPlayer().getInventory().getStack(i).getItem() instanceof BlockItem item) && isFullCubePlaceable(item.getBlock().getDefaultState());
        });
    }

    public Optional<BlockHitResult> findFirstPlacement(Vec3d vec3d, BlockPos blockPos) {
        return Arrays.stream(Direction.values()).flatMap(direction -> {
            BlockPos blockPosAdd = blockPos.add(direction.getVector());
            return Stream.concat(Stream.of(computeHitResult(vec3d, blockPos, direction)), Arrays.stream(Direction.values()).flatMap(direction2 -> {
                BlockPos blockPosAdd2 = blockPosAdd.add(direction2.getVector());
                return Stream.concat(Stream.of(computeHitResult(vec3d, blockPosAdd, direction2)), Arrays.stream(Direction.values()).map(direction3 -> {
                    return computeHitResult(vec3d, blockPosAdd2, direction3);
                }));
            }));
        }).filter((v0) -> {
            return Objects.nonNull(v0);
        }).findFirst();
    }

    public BlockHitResult computeHitResult(Vec3d vec3d, BlockPos blockPos, Direction direction) {
        BlockPos immutable = blockPos.add(direction.getVector()).toImmutable();
        Vec3d closestVec = this.hitPointResolver.getClosestVec(vec3d, getBoxPlaced(blockPos, immutable, direction));
        BlockHitResult blockHitResultRaycast = Box.raycast(List.of(Box.from(Vec3d.ZERO)), vec3d, closestVec, immutable);
        if (blockHitResultRaycast == null || !direction.equals(blockHitResultRaycast.getSide().getOpposite()) || vec3d.distanceTo(closestVec) >= 4.449999809265137d || !isBoxClear(new Box(blockPos))) {
            return null;
        }
        return new BlockHitResult(blockHitResultRaycast.getPos().add(Vec3d.of(direction.getVector()).multiply(1.0E-12d)), blockHitResultRaycast.getSide(), blockHitResultRaycast.getBlockPos(), blockHitResultRaycast.isInsideBlock());
    }

    public boolean isSupportBlock(BlockState blockState) {
        return (blockState.isReplaceable() || blockState.onUse(this.mc.getWorld(), this.mc.getPlayer(), new BlockHitResult(BlockPos.ORIGIN.toCenterPos(), Direction.SOUTH, BlockPos.ORIGIN, false)).isAccepted()) ? false : true;
    }

    public boolean isFullCubePlaceable(BlockState blockState) {
        VoxelShape collisionShape = blockState.getCollisionShape(this.mc.getWorld(), BlockPos.ORIGIN);
        return isSupportBlock(blockState) && !collisionShape.isEmpty() && collisionShape.getBoundingBox().getAverageSideLength() == 1.0d;
    }

    public boolean isBoxClear(Box box) {
        return !this.mc.getPlayer().getBoundingBox().intersects(box) && IteratorUtil.toList(this.mc.getWorld().getEntities().iterator()).stream().filter(entity -> {
            return entity instanceof LivingEntity;
        }).noneMatch(entity2 -> {
            return entity2.getBoundingBox().intersects(box);
        });
    }

    public void placeBlock(ItemStack itemStack, BlockHitResult blockHitResult) {
        PlayerActionUtil.INSTANCE.packetRotate(RotationMath.INSTANCE.fromVec3d(blockHitResult.getPos().subtract(this.mc.getPlayer().getEyePos())), 0.25f, 10.0f);
        PlayerActionUtil.INSTANCE.interactBlock(Hand.MAIN_HAND, blockHitResult);
        BlockPos blockPosAdd = blockHitResult.getBlockPos().add(blockHitResult.getSide().getVector());
        ClientWorld world = this.mc.getWorld();
        this.placedBlockPos = blockPosAdd;
        world.setBlockState(blockPosAdd, ((BlockItem) itemStack.getItem()).getBlock().getDefaultState());
        itemStack.setCount(itemStack.getCount() - 1);
    }

    public List<BlockHitResult> findPlacementPath(Vec3d vec3d, BlockPos blockPos) {
        List<List<BlockHitResult>> arrayList = new ArrayList();
        searchPlacementPath(vec3d, blockPos, new ArrayList(), arrayList, 5);
        return arrayList.stream().min(Comparator.comparing((v0) -> {
            return v0.size();
        })).orElse(new ArrayList());
    }

    public void searchPlacementPath(Vec3d vec3d, BlockPos blockPos, List<BlockHitResult> list, List<List<BlockHitResult>> list2, int i) {
        for (Direction direction : Direction.values()) {
            BlockHitResult blockHitResultMethod028 = computeHitResult(vec3d, blockPos, direction);
            if (blockHitResultMethod028 != null) {
                if (canPlaceAgainst(this.mc.getWorld().getBlockState(blockHitResultMethod028.getBlockPos()), this.mc.getWorld().getBlockState(blockPos))) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(blockHitResultMethod028);
                    arrayList.addAll(list);
                    list2.add(arrayList);
                } else if (i > 1) {
                    ArrayList arrayList2 = new ArrayList(list);
                    arrayList2.addFirst(blockHitResultMethod028);
                    searchPlacementPath(vec3d, blockHitResultMethod028.getBlockPos(), arrayList2, list2, i - 1);
                }
            }
        }
    }

    public boolean canPlaceAgainst(BlockState blockState, BlockState blockState2) {
        return isSupportBlock(blockState) && !blockState.getOutlineShape(this.mc.getWorld(), BlockPos.ORIGIN).isEmpty() && blockState2.getOutlineShape(this.mc.getWorld(), BlockPos.ORIGIN).isEmpty();
    }

    public void resetSlot(int i) {
        PacketSender.sendPacket(new UpdateSelectedSlotC2SPacket(i));
        ((BlinkModule) Expensive.INSTANCE.moduleRepository().get(BlinkModule.class)).deactivateSilent();
        this.placedBlockPos = BlockPos.ORIGIN;
    }

    public Box getBoxPlaced(BlockPos blockPos, BlockPos blockPos2, Direction direction) {
        return new Box(blockPos).union(new Box(blockPos2)).expand(-Math.max(0.05d, Math.abs(direction.getOffsetX())), -Math.max(0.05d, Math.abs(direction.getOffsetY())), -Math.max(0.05d, Math.abs(direction.getOffsetZ()))).offset(Vec3d.of(direction.getVector()).multiply(1.0E-12d));
    }

    private static double approach(double current, double target, double step) {
        if (current < target) {
            return Math.min(target, current + step);
        }
        return Math.max(target, current - step);
    }
}
