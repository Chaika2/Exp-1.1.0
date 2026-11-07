package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.util.PlayerActionUtil;
import aethereal.util.RotationManager;
import aethereal.event.UseItemEvent;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

@Aliases(aliases = {"No Interact", "Ghost Hand"})
public class NoInteractModule extends Module {
    public final Mc mc;

    public NoInteractModule() {
        super(ModuleTab.PLAYER, "No Interact");
        this.mc = Mc.INSTANCE;
        register(UseItemEvent.class, class059Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                for (Hand hand : Hand.values()) {
                    ClientPlayerEntity player = this.mc.getPlayer();
                    ItemStack stackInHand = player.getStackInHand(hand);
                    if (!player.getItemCooldownManager().isCoolingDown(stackInHand) && !stackInHand.isEmpty() && !(stackInHand.getItem() instanceof BucketItem)) {
                        if (stackInHand.use(this.mc.getWorld(), player, hand) instanceof ActionResult.Success success) {
                            PlayerActionUtil.INSTANCE.interactItem(hand, RotationManager.INSTANCE.getCurrentRotation(), false);
                            if (success.swingSource().equals(ActionResult.SwingSource.CLIENT)) {
                                player.swingHand(hand);
                            }
                            class059Var.cancel();
                        }
                    }
                }
            }
        });
    }
}
