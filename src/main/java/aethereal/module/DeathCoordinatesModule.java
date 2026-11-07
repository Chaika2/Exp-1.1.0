package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.ChatUtil;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.event.PlayerInitEvent;
import aethereal.event.PlayerTickEvent;

import java.util.concurrent.TimeUnit;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

@Aliases(aliases = {"Death Coords", "Death Coordinates", "Point Death"})
public class DeathCoordinatesModule extends Module {
    public static final String deathCoordsFormat = "%s: " + String.valueOf(Formatting.RED) + "X: " + String.valueOf(Formatting.RESET) + "%d" + String.valueOf(Formatting.RED) + " Y: " + String.valueOf(Formatting.RESET) + "%d" + String.valueOf(Formatting.RED) + " Z: " + String.valueOf(Formatting.RESET) + "%d.";
    public boolean alreadyPosted;

    public DeathCoordinatesModule() {
        super(ModuleTab.PLAYER, "Death Coordinates");
        register(PlayerInitEvent.class, class125Var -> {
            this.alreadyPosted = false;
        });
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && class130Var.isPre()) {
                Mc class815Var = Mc.INSTANCE;
                ClientPlayerEntity player = class815Var.getPlayer();
                if (!(class815Var.getCurrentScreen() instanceof DeathScreen) || this.alreadyPosted) {
                    return;
                }
                BlockPos blockPos = player.getBlockPos();
                String str = deathCoordsFormat.formatted(Lang.DEATHCOORDINATES_DEATH_TEXT.effective(), Integer.valueOf(blockPos.getX()), Integer.valueOf(blockPos.getY()), Integer.valueOf(blockPos.getZ()));
                Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(str), 5L, TimeUnit.SECONDS);
                ChatUtil.addChatMessage(str);
                this.alreadyPosted = true;
            }
        });
    }
}
