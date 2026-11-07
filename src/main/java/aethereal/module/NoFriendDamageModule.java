package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.util.FriendManager;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.event.PacketSendEvent;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

@Aliases(aliases = {"No Friend Damage", "Anti Friend Hit", "No Friendly Fire", "Friend Protection", "No Friend Attack"})
public class NoFriendDamageModule extends Module {
    public NoFriendDamageModule() {
        super(ModuleTab.COMBAT, "No Friend Damage");
        register(PacketSendEvent.class, class037Var -> {
            if (isState() && Mc.INSTANCE.isWorldLoaded()) {
                if ((class037Var.getPacket()) instanceof PlayerInteractEntityC2SPacket packet ) {
                    PlayerInteractEntityC2SPacket playerInteractEntityC2SPacket = packet;
                    Entity entityById = Mc.INSTANCE.getWorld().getEntityById(playerInteractEntityC2SPacket.entityId);
                    if (entityById == null || !isFriendAttack(entityById, playerInteractEntityC2SPacket.type.getType())) {
                        return;
                    }
                    class037Var.cancel();
                }
            }
        });
    }

    public boolean isFriendAttack(Entity entity, PlayerInteractEntityC2SPacket.InteractType interactType) {
        if (FriendManager.isFriend(entity.getName().getString()) && interactType == PlayerInteractEntityC2SPacket.InteractType.ATTACK) {
            return entity instanceof PlayerEntity;
        }
        return false;
    }
}
