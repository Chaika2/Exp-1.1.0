package aethereal.command;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.math.DirectionalInput;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.util.MovementInputHelper;
import aethereal.net.PacketSender;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HClipCommand implements ClientCommand {
    @Override
    public String getName() {
        return "hclip";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_HCLIP_DESC;
    }

    @Override
    public String getUsage() {
        return ".hclip [blocks]";
    }

    @Override
    public List<String> getAliases() {
        return List.of("hclip");
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        String[] strArrArgs = class392Var.args();
        Mc class815Var = Mc.INSTANCE;
        if (!class815Var.isWorldLoaded()) {
            System.err.println("ser vi che dalbaeb");
            return;
        }
        if (strArrArgs.length == 0) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NO_BLOCK_AMOUNT.effective().replace("{usage}", getUsage())));
        }
        if (strArrArgs.length > 1) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_TOO_MANY_ARGUMENTS.effective().replace("{usage}", getUsage())));
        }
        try {
            double d = Double.parseDouble(strArrArgs[0]);
            ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
            double radians = Math.toRadians(MovementInputHelper.getMovementDirectionOfInput(player.getYaw(), DirectionalInput.fromPlayerInput(player)));
            double dSin = Math.sin(radians);
            double dCos = Math.cos(radians);
            double d2 = (-dSin) * d;
            double d3 = dCos * d;
            Entity controllingVehicle = class815Var.getPlayer().getControllingVehicle();
            if (controllingVehicle != null) {
                controllingVehicle.setPosition(controllingVehicle.getPos().add(d2, 0.0d, d3));
            } else {
                for (int i = 0; i < 5; i++) {
                    PacketSender.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX() + d2, player.getY(), player.getZ() + d3, false, player.horizontalCollision));
                }
                player.setPosition(player.getPos().add(d2, 0.0d, d3));
                for (int i2 = 0; i2 < 5; i2++) {
                    PacketSender.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX() + d2, player.getY(), player.getZ() + d3, false, player.horizontalCollision));
                }
            }
            String[] strArrSplit = Lang.COMMAND_HCLIP_MOVED.effective().split("\\{blocks\\}");
            ChatUtil.addChatMessage((Text) Text.literal(strArrSplit[0]).formatted(Formatting.GRAY).append(Text.literal(formatBlockCount(d)).formatted(Formatting.RED)).append(Text.literal(strArrSplit.length > 1 ? strArrSplit[1] : "").formatted(Formatting.GRAY)));
        } catch (NumberFormatException e) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_NOT_A_NUMBER.effective().replace("{value}", strArrArgs[0]).replace("{usage}", getUsage())));
        }
    }

    public String formatBlockCount(double d) {
        int iRound = (int) Math.round(d);
        if (iRound % 10 != 1 || iRound % 100 == 11) {
            return (iRound % 10 < 2 || iRound % 10 > 4 || (iRound % 100 >= 10 && iRound % 100 < 20)) ? iRound + " блоков" : iRound + " блока";
        }
        return iRound + " блок";
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        return List.of();
    }
}
