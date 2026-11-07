package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.AutoLeaveCondition;
import aethereal.ui.setting.BooleanSetting;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.type.NotificationType;
import aethereal.ui.setting.NumberSetting;
import aethereal.event.PlayerTickEvent;
import aethereal.util.PvPModeDetector;
import aethereal.util.ServerUtil;
import aethereal.type.SettingUnit;
import aethereal.util.StaffDetector;
import aethereal.ui.setting.TextFieldSetting;

import java.util.concurrent.TimeUnit;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

@Aliases(aliases = {"Auto Leave", "Auto Exit", "Leave on Condition", "Server Leave", "Auto Disconnect"})
public class AutoLeaveModule extends Module {
    public final BooleanSetting disconnectServer;
    public final TextFieldSetting leaveCommand;
    public final MultiSelectSetting<AutoLeaveCondition> conditions;
    public final NumberSetting minHealth;
    public final NumberSetting leaveDistance;
    public boolean hasLeft;

    public AutoLeaveModule() {
        super(ModuleTab.MISC, "Auto Leave");
        this.disconnectServer = new BooleanSetting(Lang.AUTOLEAVE_LEAVE_SERVER);
        this.leaveCommand = (TextFieldSetting) new TextFieldSetting(Lang.AUTOLEAVE_COMMAND, Lang.AUTOLEAVE_COMMAND_DESC).setText("/hub").setPlaceholder(Lang.AUTOLEAVE_COMMAND_PLACEHOLDER).setMax(20).setVisible(() -> {
            return Boolean.valueOf(!this.disconnectServer.isValue());
        });
        this.conditions = new MultiSelectSetting(Lang.AUTOLEAVE_CONDITIONS).values(AutoLeaveCondition.class);
        this.minHealth = (NumberSetting) new NumberSetting(Lang.AUTOLEAVE_MIN_HEALTH).currentValue(5.0f).range(1.0f, 20.0f).step(0.05f).valueUnit(SettingUnit.HITPOINTS).setVisible(() -> {
            return Boolean.valueOf(this.conditions.isSelected(AutoLeaveCondition.LOW_HEALTH));
        });
        this.leaveDistance = (NumberSetting) new NumberSetting(Lang.AUTOLEAVE_LEAVE_DISTANCE).currentValue(10.0f).range(1.0f, 100.0f).step(1.0f).unit(SettingUnit.BLOCKS).setVisible(() -> {
            return Boolean.valueOf(this.conditions.isSelected(AutoLeaveCondition.DISTANCE));
        });
        addSettings(this.conditions, this.leaveCommand, this.disconnectServer, this.leaveDistance, this.minHealth);
        register(PlayerTickEvent.class, class130Var -> {
            if (class130Var.isPre()) {
                Mc class815Var = Mc.INSTANCE;
                if (isState() && class815Var.isWorldLoaded() && !class815Var.isSingleplayer()) {
                    String worldType = ServerUtil.getWorldType();
                    if (worldType.equals("lobby") || worldType.equals("world_spawn") || this.hasLeft || PvPModeDetector.isPvPMode()) {
                        return;
                    }
                    PlayerEntity player = class815Var.getPlayer();
                    ClientPlayNetworkHandler networkHandler = class815Var.getNetworkHandler();
                    ClientWorld world = class815Var.getWorld();
                    if (this.conditions.isSelected(AutoLeaveCondition.LOW_HEALTH) && player.getHealth() <= this.minHealth.currentValue()) {
                        leave(networkHandler, Lang.AUTOLEAVE_LOW_HEALTH_REASON.effective().replace("{health}", String.format("%.1f", Float.valueOf(player.getHealth()))));
                        return;
                    }
                    if (this.conditions.isSelected(AutoLeaveCondition.MODERATOR) && !StaffDetector.staffPlayers.isEmpty()) {
                        leave(networkHandler, Lang.AUTOLEAVE_MODERATOR_REASON.effective().replace("{name}", StaffDetector.staffPlayers.iterator().next().name()));
                        return;
                    }
                    if (this.conditions.isSelected(AutoLeaveCondition.DISTANCE)) {
                        for (PlayerEntity playerEntity : world.getPlayers()) {
                            if (playerEntity != player && !FriendManager.isFriend(playerEntity.getName().getString())) {
                                float fDistanceTo = playerEntity.distanceTo(player);
                                if (fDistanceTo <= this.leaveDistance.currentValue()) {
                                    leave(networkHandler, Lang.AUTOLEAVE_LEAVE_REASON.effective().replace("{distance}", String.format("%.2f", Float.valueOf(fDistanceTo))));
                                    return;
                                }
                            }
                        }
                    }
                }
            }
        });
    }

    public void leave(ClientPlayNetworkHandler clientPlayNetworkHandler, String str) {
        if (this.disconnectServer.isValue()) {
            clientPlayNetworkHandler.getConnection().disconnect(Text.of("[AutoLeave] " + str));
        } else {
            String text = this.leaveCommand.getText();
            if (!text.isEmpty()) {
                clientPlayNetworkHandler.sendChatCommand(text.substring(1));
                Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(str), 15L, TimeUnit.SECONDS);
            }
        }
        this.hasLeft = true;
        switchState();
    }

    @Override
    public void deactivate() {
        this.hasLeft = false;
        super.deactivate();
    }
}
