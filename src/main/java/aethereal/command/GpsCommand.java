package aethereal.command;
import aethereal.module.ArrowsModule;
import aethereal.util.ChatUtil;
import aethereal.model.CommandContext;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.model.TranslatedException;
import aethereal.model.Translation;

import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class GpsCommand implements ClientCommand {
    @Override
    public String getName() {
        return "gps";
    }

    @Override
    public Translation getDescription() {
        return Lang.COMMAND_GPS_DESC;
    }

    @Override
    public String getUsage() {
        return ".gps <x> <z>\n.gps off";
    }

    @Override
    public List<String> getAliases() {
        return List.of();
    }

    @Override
    public void execute(CommandContext class392Var) throws TranslatedException {
        String[] strArrArgs = class392Var.args();
        if (!Mc.INSTANCE.isWorldLoaded()) {
            throw new TranslatedException(Lang.COMMAND_WORLD_NOT_LOADED);
        }
        ClientPlayerEntity player = Mc.INSTANCE.getPlayer();
        ArrowsModule class535VarMethod001 = getArrowsModule();
        if (strArrArgs.length == 1 && strArrArgs[0].equalsIgnoreCase("off")) {
            class535VarMethod001.disableGPS();
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_GPS_DISABLED.effective()).formatted(Formatting.GRAY));
        } else {
            if (strArrArgs.length != 2) {
                throw new TranslatedException(Translation.clearText(Lang.COMMAND_INVALID_ARG_COUNT.effective().replace("{usage}", getUsage())));
            }
            int iMethod002 = parseCoordinate(strArrArgs[0], player.getX());
            int iMethod003 = parseCoordinate(strArrArgs[1], player.getZ());
            class535VarMethod001.setGPS(iMethod002, iMethod003);
            ChatUtil.addChatMessage((Text) Text.literal(Lang.COMMAND_GPS_ENABLED.effective().replace("{x}", String.valueOf(iMethod002)).replace("{z}", String.valueOf(iMethod003))).formatted(Formatting.GRAY));
        }
    }

    public int parseCoordinate(String str, double d) throws TranslatedException {
        if (str.equals("~")) {
            return (int) Math.floor(d);
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new TranslatedException(Translation.clearText(Lang.COMMAND_INVALID_COORDINATE.effective().replace("{input}", str)));
        }
    }

    public ArrowsModule getArrowsModule() {
        return (ArrowsModule) Expensive.INSTANCE.moduleRepository().get(ArrowsModule.class);
    }

    @Override
    public List<String> getSuggestions(String[] strArr, int i) {
        return (i != 0 || strArr.length > 1) ? List.of("~") : List.of("off", "~");
    }
}
