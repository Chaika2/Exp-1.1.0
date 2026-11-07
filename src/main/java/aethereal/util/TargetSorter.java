package aethereal.util;

import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

public interface TargetSorter {
    List<LivingEntity> sort(List<LivingEntity> list, ClientPlayerEntity clientPlayerEntity);

    default TargetSorter then(TargetSorter class004Var) {
        return (list, clientPlayerEntity) -> {
            return class004Var.sort(sort(list, clientPlayerEntity), clientPlayerEntity);
        };
    }
}
