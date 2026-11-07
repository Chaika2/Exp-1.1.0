package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.net.PacketSender;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.List;
import java.util.stream.Stream;
import aethereal.util.InventoryUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class VClipCommand implements ClientCommand {
    @Override
    public String getName() {
        return "vclip";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_VCLIP_DESC;
    }

    @Override
    public String getUsage() {
        return ".vclip up/down/[blocks]";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        float fMethod003;
        String[] strArrArgs = class392Var.args();
        Mc class815Var = Mc.INSTANCE;
        if (!class815Var.isWorldLoaded()) {
            System.err.println("ser vi che dalbaeb");
            return;
        }
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_DIRECTION_OR_DISTANCE.effective().replace("{usage}", getUsage())));
        }
        if (strArrArgs.length > 1) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_TOO_MANY_ARGUMENTS.effective().replace("{usage}", getUsage())));
        }
        ClientPlayerEntity player = class815Var.getPlayer();
        BlockPos blockPos = player.getBlockPos();
        String str = strArrArgs[0];
        switch (str) {
            case "up":
                fMethod003 = findGap(blockPos, true);
                break;
            case "down":
                fMethod003 = findGap(blockPos, false);
                break;
            default:
                fMethod003 = parseDistance(str);
                break;
        }
        if (fMethod003 == 0.0f) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_VCLIP_FAILED.effective()).formatted(Formatting.GRAY));
            return;
        }
        performVclip(player, fMethod003);
        String[] strArrSplit = Lang.COMMAND_VCLIP_MOVED.effective().split("\\{blocks\\}");
        ChatUtil.addChatMessage((Text) Text.literal(strArrSplit[0]).formatted(Formatting.GRAY).append(Text.literal(pluralizeBlocks(fMethod003)).formatted(Formatting.RED)).append(Text.literal(strArrSplit.length > 1 ? strArrSplit[1] : "").formatted(Formatting.GRAY)));
    }

    public String pluralizeBlocks(double d) {
        int iRound = (int) Math.round(d);
        if (iRound % 10 != 1 || iRound % 100 == 11) {
            return (iRound % 10 < 2 || iRound % 10 > 4 || (iRound % 100 >= 10 && iRound % 100 < 20)) ? iRound + " блоков" : iRound + " блока";
        }
        return iRound + " блок";
    }

    public void performVclip(ClientPlayerEntity clientPlayerEntity, float f) {
        int iElytraSlot = InventoryUtil.INSTANCE.findItemSlot(Items.ELYTRA, true);
        boolean zHasElytra = iElytraSlot != -1 && iElytraSlot != -2;
        if (zHasElytra) {
            swapElytra(iElytraSlot, true);
        }
        teleport(clientPlayerEntity, f, zHasElytra);
        if (zHasElytra) {
            swapElytra(iElytraSlot, false);
        }
    }

    public void teleport(ClientPlayerEntity clientPlayerEntity, float f, boolean z) {
        double dX = clientPlayerEntity.getX();
        double dY = clientPlayerEntity.getY();
        double dZ = clientPlayerEntity.getZ();
        if (z) {
            for (int i = 0; i < 2; i++) {
                clientPlayerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(dX, dY, dZ, false, clientPlayerEntity.horizontalCollision));
            }
            clientPlayerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(clientPlayerEntity, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            clientPlayerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(dX, dY + ((double) f), dZ, false, clientPlayerEntity.horizontalCollision));
            clientPlayerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(clientPlayerEntity, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        } else {
            int iMethod002 = packetCount(f);
            for (int i = 0; i < iMethod002; i++) {
                clientPlayerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(clientPlayerEntity.isOnGround(), clientPlayerEntity.horizontalCollision));
            }
            clientPlayerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(dX, dY + ((double) f), dZ, false, clientPlayerEntity.horizontalCollision));
        }
        clientPlayerEntity.setPosition(clientPlayerEntity.getPos().add(0.0d, ((double) f), 0.0d));
        clientPlayerEntity.fallDistance = 0.0f;
    }

    public void swapElytra(int i, boolean z) {
        final int iChestSlot = 6;
        if (z) {
            InventoryUtil.INSTANCE.windowClick(SlotActionType.PICKUP, i, 0);
            InventoryUtil.INSTANCE.windowClick(SlotActionType.PICKUP, iChestSlot, 0);
        } else {
            InventoryUtil.INSTANCE.windowClick(SlotActionType.PICKUP, iChestSlot, 0);
            InventoryUtil.INSTANCE.windowClick(SlotActionType.PICKUP, i, 0);
        }
    }

    public float parseDistance(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_NOT_A_NUMBER.effective().replace("{value}", str).replace("{usage}", getUsage())).formatted(Formatting.GRAY));
            return 0.0f;
        }
    }

    public int findGap(BlockPos blockPos, boolean z) {
        ClientWorld world = Mc.INSTANCE.getWorld();
        int bottomY = world.getBottomY();
        int topYInclusive = world.getTopYInclusive();
        int i = z ? 3 : -1;
        int y = z ? topYInclusive - blockPos.getY() : bottomY - blockPos.getY();
        int i2 = z ? 1 : -1;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        int i3 = i;
        while (true) {
            int i4 = i3;
            if (z) {
                if (i4 > y) {
                    return 0;
                }
            } else if (i4 < y) {
                return 0;
            }
            mutable.set(blockPos.getX(), blockPos.getY() + i4, blockPos.getZ());
            BlockState blockState = world.getBlockState(mutable);
            if (blockState.isAir() && world.getBlockState(mutable.up()).isAir()) {
                return i4;
            }
            if (!z && blockState.isOf(Blocks.BEDROCK)) {
                ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_VCLIP_BEDROCK.effective()).formatted(Formatting.RED));
                return 0;
            }
            i3 = i4 + i2;
        }
    }

    public int packetCount(float f) {
        return Math.max((int) (f / 1000.0f), 3);
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        return i == 0 ? Stream.of(new String[]{"down", "up"}).filter(str -> {
            return str.startsWith(strArr[0].toLowerCase());
        }).toList() : List.of();
    }
}
