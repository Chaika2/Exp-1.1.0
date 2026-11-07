package aethereal.util;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.net.PacketSender;
import aethereal.math.Rotation;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2f;

public class PlayerActionUtil {
    public static final PlayerActionUtil INSTANCE = new PlayerActionUtil();

    public OptionalInt hotbarSlotsStream(IntPredicate intPredicate) {
        return IntStream.range(0, 9).filter(intPredicate).findFirst();
    }

    public float getRemainingCooldownSeconds(Item item) {
        Mc class815Var = Mc.INSTANCE;
        ItemCooldownManager itemCooldownManager = class815Var.getPlayer().getItemCooldownManager();
        ItemCooldownManager.Entry entry = (ItemCooldownManager.Entry) itemCooldownManager.entries.get(Registries.ITEM.getId(item));
        if (entry == null) {
            return 0.0f;
        }
        return Math.max(1, (int) Math.ceil(itemCooldownManager.getCooldownProgress(item.getDefaultStack(), class815Var.getMinecraft().getRenderTickCounter().getTickDelta(false)) * (entry.endTick - entry.startTick))) / 20.0f;
    }

    public void interactItem(Hand hand, Rotation class007Var, boolean z) {
        Mc class815Var = Mc.INSTANCE;
        ClientPlayerEntity player = class815Var.getPlayer();
        Optional.of(Integer.valueOf(ServerUtil.getProtocolVersion())).filter(num -> {
            return num.intValue() > 754 && num.intValue() < 767;
        }).ifPresent(num2 -> {
            player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), class007Var.getYaw(), class007Var.getPitch(), player.isOnGround(), false));
        });
        Expensive.INSTANCE.inventoryService().itemInteractor().interactItem(player, hand, class007Var);
        if (z) {
            class815Var.getPlayer().networkHandler.sendPacket(new HandSwingC2SPacket(hand));
        }
    }

    public int getEmptySlots(PlayerInventory playerInventory) {
        int i = 0;
        for (int i2 = 0; i2 < 36; i2++) {
            if (playerInventory.getStack(i2).isEmpty()) {
                i++;
            }
        }
        return i;
    }

    public void packetRotate(Rotation class007Var, float f, float f2) {
        packetRotate(class007Var, f, new Vector2f(1.0f), f2);
    }

    public void packetRotate(Rotation class007Var, float f, Vector2f vector2f, float f2) {
        Rotation serverRotation = RotationManager.INSTANCE.getServerRotation();
        float random = f;
        while (true) {
            float f3 = random;
            if (f3 >= 1.0f) {
                moveBypass$$$(class007Var.normalize());
                return;
            } else {
                moveBypass$$$(new Rotation(MathUtil.lerp(f3, serverRotation.getYaw(), class007Var.getYaw()), MathUtil.lerp(f3, serverRotation.getPitch(), class007Var.getPitch())).random(f2).normalize());
                random = f3 + (f * MathUtil.getRandom(vector2f.x, vector2f.y));
            }
        }
    }

    public void moveBypass$$$(Rotation class007Var) {
        moveBypass$$$(0.0d, class007Var, Mc.INSTANCE.getPlayer().isOnGround());
    }

    public void moveBypass$$$(double d, Rotation class007Var, boolean z) {
        moveBypass$$$(Mc.INSTANCE.getPlayer().getPos().add(0.0d, d, 0.0d), class007Var, z);
    }

    public void moveBypass$$$(Vec3d vec3d, Rotation class007Var, boolean z) {
        Mc.INSTANCE.getPlayer().networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(vec3d.getX(), vec3d.getY(), vec3d.getZ(), class007Var.getYaw(), class007Var.getPitch(), z, !Mc.INSTANCE.getPlayer().horizontalCollision));
        Mc.INSTANCE.getPlayer().setPosition(vec3d);
    }

    public void interactBlock(Hand hand, BlockHitResult blockHitResult) {
        PacketSender.sendSequencedNotSilentPacket(i -> {
            return new PlayerInteractBlockC2SPacket(hand, blockHitResult, i);
        });
    }

    public Slot mainHandSlot() {
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        DefaultedList defaultedList = player.currentScreenHandler.slots;
        long size = defaultedList.size();
        return (Slot) defaultedList.get(Math.toIntExact((size - ((long) (size == 46 ? 10 : 9))) + ((long) player.getInventory().selectedSlot)));
    }

    public void swapHand(int i, Hand hand) {
        if (i == -1) {
            return;
        }
        windowClick(SlotActionType.SWAP, i, hand.equals(Hand.MAIN_HAND) ? Mc.INSTANCE.getPlayer().getInventory().selectedSlot : 40);
    }

    public void clickSlot(int i, int i2, int i3, SlotActionType slotActionType, boolean z) {
        Mc class815Var = Mc.INSTANCE;
        ScreenHandler currentScreenHandler = class815Var.getCurrentScreenHandler();
        if (!z) {
            currentScreenHandler.onSlotClick(i2, i3, slotActionType, class815Var.getPlayer());
        }
        class815Var.getPlayer().networkHandler.sendPacket(new ClickSlotC2SPacket(i, currentScreenHandler.getRevision(), i2, i3, slotActionType, currentScreenHandler.getCursorStack().copy(), Int2ObjectMaps.emptyMap()));
    }

    public void swapHand(int i, Hand hand, boolean z, boolean z2) {
        if (i == -1) {
            return;
        }
        windowClick(SlotActionType.SWAP, i, hand.equals(Hand.MAIN_HAND) ? Mc.INSTANCE.getPlayer().getInventory().selectedSlot : 40, !z && z2);
        if (z) {
            updateSlots(z2);
        }
    }

    public void swapHand(int i, Hand hand, boolean z) {
        if (i == -1) {
            return;
        }
        windowClick(SlotActionType.SWAP, i, hand.equals(Hand.MAIN_HAND) ? Mc.INSTANCE.getPlayer().getInventory().selectedSlot : 40);
        if (z) {
            updateSlots(false);
        }
    }

    public void swapHand(int i, int i2, boolean z) {
        if (i == -1) {
            return;
        }
        windowClick(SlotActionType.SWAP, i, i2);
        if (z) {
            updateSlots(false);
        }
    }

    public void swapHand(int i, int i2, boolean z, boolean z2) {
        if (i == -1) {
            return;
        }
        windowClick(SlotActionType.SWAP, i, i2, !z && z2);
        if (z) {
            updateSlots(z2);
        }
    }

    public void updateSlots(boolean z) {
        if (!z || ServerUtil.getProtocolVersion() < 755) {
            return;
        }
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        if (player == null) {
            return;
        }
        // Здесь НЕЛЬЗЯ слать ClickSlotC2SPacket с PICKUP_ALL. Раньше он уходил с
        // revision=0, и сервер его отбрасывал по несовпадению ревизии. Стоило
        // подставить настоящий revision - сервер стал применять клик: забирать
        // стак из слота 0 в курсор и подкладывать предмет, которого там не было
        // (там стоял СЛУЧАЙНЫЙ предмет из Registries.ITEM). Локально onSlotClick
        // при этом НЕ вызывался, поэтому клиент предсказание не применял и получал
        // расхождение. Сервер в ответ заливал InventoryS2CPacket +
        // ScreenHandlerSlotUpdateS2CPacket - из-за этого тотем и выглядел
        // "только визуальным": клиент рисовал свап, сервер состояние не менял.
        // Форс-ресинк делается одним CloseHandledScreen, отдельный клик не нужен.
        player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(player.currentScreenHandler.syncId));
    }

    public void windowClick(SlotActionType slotActionType, int i, int i2, boolean z) {
        Mc class815Var;
        ClientPlayerEntity player;
        if (i == -1 || (player = (class815Var = Mc.INSTANCE).getPlayer()) == null) {
            return;
        }
        int i3 = player.currentScreenHandler.syncId;
        class815Var.getInteractionManager().clickSlot(i3, i, i2, slotActionType, player);
        System.out.println("windowClick successful: Slot ID = " + i + ", Button = " + i2 + ", Type = " + String.valueOf(slotActionType));
        if (z) {
            player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(i3));
        }
    }

    public void windowClick(SlotActionType slotActionType, int i, int i2) {
        Mc class815Var = Mc.INSTANCE;
        ClientPlayerEntity player = class815Var.getPlayer();
        if (player == null) {
            return;
        }
        // Никакого вывода в лог здесь: println между кликами блокирует render
        // thread и растягивает пачку кликов, из-за чего сервер успевает прислать
        // ответы между кликами. В эталонных клиентах (srcrock InventoryScreenMixin,
        // Zenith MixinHandledScreen) здесь тоже пусто.
        class815Var.getInteractionManager().clickSlot(player.currentScreenHandler.syncId, i, i2, slotActionType, player);
    }
}
