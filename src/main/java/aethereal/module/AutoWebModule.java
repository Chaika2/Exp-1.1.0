package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.AutoWebMode;
import aethereal.util.BlockUtil;
import aethereal.ui.setting.BooleanSetting;
import aethereal.ui.setting.ColorSetting;
import aethereal.render.ColorStack;
import aethereal.type.CombatPauseManager;
import aethereal.math.Easings;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.util.GrimDelayHandler;
import aethereal.util.HitPointResolver;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.ui.setting.NumberSetting;
import aethereal.net.PacketSender;
import aethereal.util.PlayerActionUtil;
import aethereal.math.Rotation;
import aethereal.util.RotationManager;
import aethereal.event.RotationUpdateEvent;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;
import aethereal.render.ShapeRenderer;
import aethereal.math.SimulatedPlayer;
import aethereal.event.WorldRenderEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

@Aliases(aliases = {"Auto Web", "Web Trap", "Cobweb Placer", "Auto Cobweb"})
public class AutoWebModule extends Module {
    public final Mc mc;
    public final HitPointResolver hitPointResolver;
    public final ModeSetting<AutoWebMode> mode;

    public final KeybindSetting placeKey;
    public final NumberSetting predictTicks;
    public final BooleanSetting multiPlace;
    public final ColorSetting color;
    public final Map<BlockPos, Long> placedWebs;
    public boolean versionWarningShown;
    public int pendingSlot;

    public BlockHitResult pendingHitResult;
    public boolean restoreSlotPending;

    public AutoWebModule() {
        super(ModuleTab.COMBAT, "Auto Web");
        this.mc = Mc.INSTANCE;
        this.hitPointResolver = new HitPointResolver();
        this.mode = new ModeSetting(Lang.MODE).values(AutoWebMode.class);
        this.placeKey = new KeybindSetting(Lang.AUTOWEB_PLACEKEY);
        this.predictTicks = new NumberSetting(Lang.AUTOWEB_PREDICT_TICKS, Lang.AUTOWEB_PREDICT_TICKS_DESC).currentValue(7.0f).range(1.0f, 15.0f).unit(SettingUnit.TICKS).step(1.0f);
        this.multiPlace = new BooleanSetting(Lang.AUTOWEB_MULTIPLACE, Lang.AUTOWEB_MULTIPLACE_DESC).setValue(true).visible(() -> {
            return Boolean.valueOf(this.mode.isSelected(AutoWebMode.GRIM));
        });
        this.color = new ColorSetting(Lang.SKELETON_COLOR).value(-1);
        this.placedWebs = new HashMap();
        this.pendingSlot = -1;
        addSettings(this.mode, this.placeKey, this.predictTicks, this.multiPlace, this.color);
        register(RotationUpdateEvent.class, class345Var -> {
            int protocolVersion;
            if (isState() && this.mc.isWorldLoaded() && class345Var.isPre()) {
                this.placedWebs.values().removeIf(l -> {
                    return System.currentTimeMillis() - l.longValue() > 400;
                });
                boolean zIsSelected = this.mode.isSelected(AutoWebMode.GRIM);
                if (this.restoreSlotPending) {
                    this.restoreSlotPending = false;
                    PacketSender.sendPacket(new UpdateSelectedSlotC2SPacket(this.mc.getPlayer().getInventory().selectedSlot));
                }
                if (this.pendingSlot != -1) {
                    GrimDelayHandler.rotateToAngle(Rotation.lookingAt(this.pendingHitResult.getPos(), this.mc.getPlayer().getEyePos()).random(0.5f));
                    placeWeb(this.pendingSlot, this.pendingHitResult);
                    this.pendingSlot = -1;
                    this.pendingHitResult = null;
                    this.restoreSlotPending = true;
                    return;
                }
                if (isPlaceKeyPressed()) {
                    if (!zIsSelected || ((protocolVersion = ServerUtil.getProtocolVersion()) >= 755 && protocolVersion <= 766)) {
                        PlayerActionUtil.INSTANCE.hotbarSlotsStream(i -> {
                            return this.mc.getPlayer().getInventory().getStack(i).isOf(Items.COBWEB);
                        }).ifPresent(i2 -> {
                            placeWebs(i2, zIsSelected);
                        });
                    } else {
                        if (this.versionWarningShown) {
                            return;
                        }
                        this.versionWarningShown = true;
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.ERROR, (Text) Text.literal("[Auto Web] Нужна версия " + String.valueOf(Formatting.RED) + "1.17-1.20.6"), 2L, TimeUnit.SECONDS);
                        setState(false);
                    }
                }
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (!isState() || this.placedWebs.isEmpty()) {
                return;
            }
            Matrix4f positionMatrix = class016Var.matrixStack().peek().getPositionMatrix();
            ColorStack class115VarColorStack = Expensive.INSTANCE.drawEngine().colorStack();
            long jCurrentTimeMillis = System.currentTimeMillis();
            for (Map.Entry<BlockPos, Long> entry : this.placedWebs.entrySet()) {
                long jLongValue = jCurrentTimeMillis - entry.getValue().longValue();
                if (jLongValue <= 400) {
                    float fEase = jLongValue < 200 ? Easings.EASE_OUT_CUBIC.ease(jLongValue / 200.0f) : 1.0f - Easings.EASE_OUT_CUBIC.ease((jLongValue - 200.0f) / 200.0f);
                    if (fEase >= 0.01f) {
                        BlockPos key = entry.getKey();
                        double x = ((double) key.getX()) + 0.5d;
                        double y = ((double) key.getY()) + 0.5d;
                        double z = ((double) key.getZ()) + 0.5d;
                        double d = ((double) fEase) * 0.5d;
                        Box box = new Box(x - d, y - d, z - d, x + d, y + d, z + d);
                        int red = this.color.getRed();
                        int green = this.color.getGreen();
                        int blue = this.color.getBlue();
                        int iComputeColor = class115VarColorStack.computeColor((int) (30.0f * fEase), red, green, blue);
                        int iComputeColor2 = class115VarColorStack.computeColor((int) (200.0f * fEase), red, green, blue);
                        ShapeRenderer.INSTANCE.addBox(positionMatrix, box, iComputeColor);
                        ShapeRenderer.INSTANCE.addOutline(positionMatrix, box, iComputeColor2, 1.5f);
                    }
                }
            }
        });
    }

    public void placeWebs(int i, boolean z) {
        ClientPlayerEntity player = this.mc.getPlayer();
        Vec3d eyePos = player.getEyePos();
        AtomicInteger atomicInteger = new AtomicInteger();
        int i2 = (this.multiPlace.isValue() && z) ? 4 : 1;
        ((ClientWorld) Objects.requireNonNull(this.mc.getWorld())).getPlayers().stream().filter(abstractClientPlayerEntity -> {
            return (player.equals(abstractClientPlayerEntity) || FriendManager.isFriend(abstractClientPlayerEntity.getName().getString())) ? false : true;
        }).map(abstractClientPlayerEntity2 -> {
            return SimulatedPlayer.simulateOtherPlayer(abstractClientPlayerEntity2, (int) this.predictTicks.currentValue());
        }).sorted(Comparator.comparing(class136Var -> {
            return Boolean.valueOf(!BlockUtil.checkBlockIntersection(class136Var.boundingBox, block -> {
                return block == Blocks.COBWEB;
            }));
        })).forEach(class136Var2 -> {
            if (atomicInteger.get() >= i2) {
                return;
            }
            BlockPos.stream(class136Var2.boundingBox.expand(-0.01d)).filter(blockPos -> {
                return this.mc.getWorld().getBlockState(blockPos).isAir();
            }).forEach(blockPos2 -> {
                if (atomicInteger.get() >= i2) {
                    return;
                }
                Arrays.stream(Direction.values()).map(direction -> {
                    return computePlacement(eyePos, blockPos2, direction);
                }).filter((v0) -> {
                    return Objects.nonNull(v0);
                }).findFirst().ifPresent(blockHitResult -> {
                    if (atomicInteger.get() >= i2) {
                        return;
                    }
                    Rotation class007VarRandom = Rotation.lookingAt(blockHitResult.getPos(), eyePos).random(0.5f);
                    if (z) {
                        PlayerActionUtil.INSTANCE.packetRotate(class007VarRandom, 0.2f, 5.0f);
                        placeWeb(i, blockHitResult);
                        CombatPauseManager.INSTANCE.pauseCombatForAtLeast(1);
                    } else {
                        GrimDelayHandler.rotateToAngle(class007VarRandom);
                        this.pendingSlot = i;
                        this.pendingHitResult = blockHitResult;
                        CombatPauseManager.INSTANCE.pauseCombatForAtLeast(3);
                    }
                    atomicInteger.incrementAndGet();
                });
            });
        });
        if (!z || atomicInteger.get() <= 0) {
            return;
        }
        PlayerActionUtil.INSTANCE.packetRotate(RotationManager.INSTANCE.getCurrentRotation(), 0.2f, 10.0f);
        PacketSender.sendPacket(new UpdateSelectedSlotC2SPacket(player.getInventory().selectedSlot));
    }

    public BlockHitResult computePlacement(Vec3d vec3d, BlockPos blockPos, Direction direction) {
        BlockPos blockPosAdd = blockPos.add(direction.getVector());
        if (this.placedWebs.containsKey(blockPosAdd) || this.mc.getWorld().getBlockState(blockPosAdd).getOutlineShape(this.mc.getWorld(), blockPosAdd).isEmpty()) {
            return null;
        }
        Vec3d closestVec = this.hitPointResolver.getClosestVec(vec3d, getBoxPlaced(blockPos, blockPosAdd, direction));
        BlockHitResult blockHitResultRaycast = Box.raycast(List.of(Box.from(Vec3d.ZERO)), vec3d, closestVec, blockPosAdd);
        if (blockHitResultRaycast == null || !direction.equals(blockHitResultRaycast.getSide().getOpposite()) || vec3d.distanceTo(closestVec) >= 4.400000095367432d) {
            return null;
        }
        return blockHitResultRaycast;
    }

    public void placeWeb(int i, BlockHitResult blockHitResult) {
        ItemStack stack = this.mc.getPlayer().getInventory().getStack(i);
        if (stack.isEmpty() || !stack.isOf(Items.COBWEB)) {
            return;
        }
        PacketSender.sendPacket(new UpdateSelectedSlotC2SPacket(i));
        PlayerActionUtil.INSTANCE.interactBlock(Hand.MAIN_HAND, blockHitResult);
        BlockPos blockPosAdd = blockHitResult.getBlockPos().add(blockHitResult.getSide().getVector());
        this.mc.getWorld().setBlockState(blockPosAdd, Blocks.COBWEB.getDefaultState());
        stack.setCount(stack.getCount() - 1);
        this.placedWebs.put(blockPosAdd, Long.valueOf(System.currentTimeMillis()));
    }

    public Box getBoxPlaced(BlockPos blockPos, BlockPos blockPos2, Direction direction) {
        return new Box(blockPos).union(new Box(blockPos2)).expand(-Math.max(0.05d, Math.abs(direction.getOffsetX())), -Math.max(0.05d, Math.abs(direction.getOffsetY())), -Math.max(0.05d, Math.abs(direction.getOffsetZ()))).offset(Vec3d.of(direction.getVector()).multiply(1.0E-12d));
    }

    public boolean isPlaceKeyPressed() {
        int key;
        if (this.mc.getMinecraft().currentScreen != null || (key = this.placeKey.getKey()) == -1) {
            return false;
        }
        long handle = this.mc.getWindow().getHandle();
        if (key < 8) {
            return GLFW.glfwGetMouseButton(handle, key) == 1;
        }
        return GLFW.glfwGetKey(handle, key) == 1;
    }

    @Override
    public void deactivate() {
        this.placedWebs.clear();
        this.versionWarningShown = false;
        this.pendingSlot = -1;
        this.pendingHitResult = null;
        this.restoreSlotPending = false;
        super.deactivate();
    }
}
