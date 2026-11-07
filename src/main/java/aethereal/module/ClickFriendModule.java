package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.Expensive;
import aethereal.util.FriendManager;
import aethereal.ui.setting.KeybindSetting;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;

import java.util.concurrent.TimeUnit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;

@Aliases(aliases = {"Click Friend", "Friend Manager", "Add Friend", "Remove Friend", "Toggle Friend"})
public class ClickFriendModule extends Module {
    public final KeybindSetting keybind;

    public ClickFriendModule() {
        super(ModuleTab.MISC, "Click Friend");
        this.keybind = new KeybindSetting(Lang.CLICKFRIEND_KEY);
        addSettings(this.keybind);
        this.keybind.consumer(class664Var -> {
            Mc class815Var = Mc.INSTANCE;
            Expensive.LOGGER.info("[DBG] ClickFriend consumer state={} worldLoaded={} target={}", isState(), class815Var.isWorldLoaded(), class815Var.getCrosshairTarget());
            if (isState() && class815Var.isWorldLoaded()) {
                if ((class815Var.getCrosshairTarget()) instanceof EntityHitResult crosshairTarget ) {
                    if ((crosshairTarget.getEntity()) instanceof PlayerEntity entity ) {
                        String string = entity.getName().getString();
                        String str = String.valueOf(Formatting.RED) + string + String.valueOf(Formatting.RESET);
                        if (FriendManager.isFriend(string)) {
                            FriendManager.remove(string);
                            Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.CLICKFRIEND_REMOVED.effective().replace("{name}", str)), 3L, TimeUnit.SECONDS);
                        } else {
                            FriendManager.add(string);
                            Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.CLICKFRIEND_ADDED.effective().replace("{name}", str)), 3L, TimeUnit.SECONDS);
                        }
                    }
                }
            }
        });
    }
}
