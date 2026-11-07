package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.BooleanSetting;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketReceiveEvent;
import aethereal.util.PvPModeDetector;

import java.util.Locale;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.StringHelper;

@Aliases(aliases = {"TP Accept", "Auto TP Accept", "Teleport Accept", "Auto TPA", "TP Accept Helper"})
public class TPAcceptModule extends Module {
    public final BooleanSetting onlyFriends;

    public TPAcceptModule() {
        super(ModuleTab.MISC, "TP Accept");
        this.onlyFriends = new BooleanSetting(Lang.AUTOTPACCEPT_ONLYFRIENDS, Lang.AUTOTPACCEPT_ONLYFRIENDS_DESC);
        addSettings(this.onlyFriends);
        register(PacketReceiveEvent.class, class051Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (isState() && class815Var.isWorldLoaded() && !PvPModeDetector.isPvPMode()) {
                ClientPlayNetworkHandler networkHandler = class815Var.getNetworkHandler();
                if ((class051Var.getPacket()) instanceof GameMessageS2CPacket packet ) {
                    String lowerCase = StringHelper.stripTextFormat(packet.content().getString()).toLowerCase();
                    if (lowerCase.contains("телепортироваться") || lowerCase.contains("has requested teleport") || lowerCase.contains("/tpyes")) {
                        if (!this.onlyFriends.isValue() || isFromFriend(lowerCase)) {
                            networkHandler.sendChatCommand("tpaccept");
                        }
                    }
                }
            }
        });
    }

    public boolean isFromFriend(String str) {
        NameProtectModule class512Var = (NameProtectModule) Expensive.INSTANCE.moduleRepository().get(NameProtectModule.class);
        for (String str2 : FriendManager.getFriends()) {
            if (str.contains(str2.toLowerCase(Locale.ROOT))) {
                return true;
            }
            if (class512Var != null && str.contains(class512Var.replace(str2).toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }
}
