package aethereal.type;

import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.mob.CreeperEntity;

public class CreeperFarmApproachPhase {
    public void tickApproachPhase(ClientPlayerEntity clientPlayerEntity, List<CreeperEntity> list) {
        if (list.isEmpty()) {
            return;
        }
        CreeperEntity creeperEntity = (CreeperEntity) list.getFirst();
        double dDistanceTo = clientPlayerEntity.distanceTo(creeperEntity);
        if (dDistanceTo >= 4.5d && dDistanceTo > 4.5d) {
            creeperEntity.getPos().add(clientPlayerEntity.getPos().subtract(creeperEntity.getPos()).normalize().multiply(4.5d));
        }
    }
}
