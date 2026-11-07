package aethereal.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FovSorter implements TargetSorter {
    @Override
    public List<LivingEntity> sort(List<LivingEntity> list, ClientPlayerEntity clientPlayerEntity) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        list.sort(Comparator.comparingDouble(livingEntity -> {
            return computeFovAngle(livingEntity, clientPlayerEntity);
        }));
        return list;
    }

    public double computeFovAngle(LivingEntity livingEntity, ClientPlayerEntity clientPlayerEntity) {
        Vec3d vec3dSubtract = livingEntity.getEyePos().subtract(clientPlayerEntity.getEyePos());
        double dWrapDegrees = MathHelper.wrapDegrees(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec3dSubtract.z, vec3dSubtract.x)) - 90.0d) - ((double) clientPlayerEntity.getYaw()));
        if (Math.abs(dWrapDegrees) > 180.0d) {
            dWrapDegrees -= Math.signum(dWrapDegrees) * 360.0d;
        }
        return Math.abs(dWrapDegrees);
    }
}
