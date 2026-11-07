package aethereal.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;

public class DistanceSorter implements TargetSorter {
    @Override
    public List<LivingEntity> sort(List<LivingEntity> list, ClientPlayerEntity clientPlayerEntity) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        list.sort(Comparator.comparingDouble(livingEntity -> {
            return livingEntity.distanceTo(clientPlayerEntity);
        }));
        return list;
    }
}
