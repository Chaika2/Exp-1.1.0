package aethereal.util;
import aethereal.type.Mc;
import aethereal.math.Rotation;

import java.util.Objects;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.mutable.MutableObject;

public class ItemInteractionHelper {
    public ActionResult interactItem(PlayerEntity playerEntity, Hand hand, Rotation class007Var) {
        ClientPlayerInteractionManager interactionManager = Mc.INSTANCE.getInteractionManager();
        if (interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) {
            return ActionResult.PASS;
        }
        interactionManager.syncSelectedSlot();
        ClientPlayerEntity player = (ClientPlayerEntity) playerEntity;
        MutableObject mutableObject = new MutableObject();
        interactionManager.sendSequencedPacket(Mc.INSTANCE.getWorld(), i -> {
            PlayerInteractItemC2SPacket packet = new PlayerInteractItemC2SPacket(hand, i, class007Var.getYaw(), class007Var.getPitch());
            ItemStack stackInHand = player.getStackInHand(hand);
            if (player.getItemCooldownManager().isCoolingDown(stackInHand)) {
                mutableObject.setValue(ActionResult.PASS);
                return packet;
            }
            ActionResult result = stackInHand.use(Mc.INSTANCE.getWorld(), player, hand);
            if (result instanceof ActionResult.Success success) {
                ItemStack stackInHand2 = (ItemStack) Objects.requireNonNullElseGet(success.getNewHandStack(), () -> {
                    return player.getStackInHand(hand);
                });
                if (stackInHand2 != stackInHand) {
                    player.setStackInHand(hand, stackInHand2);
                }
            }
            mutableObject.setValue(result);
            return packet;
        });
        return (ActionResult) mutableObject.getValue();
    }
}
