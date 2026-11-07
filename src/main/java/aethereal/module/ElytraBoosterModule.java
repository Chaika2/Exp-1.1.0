package aethereal.module;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.math.Rotation;
import aethereal.util.RotationManager;
import aethereal.event.TravelEvent;

import net.minecraft.util.math.Vec3d;

public class ElytraBoosterModule extends Module {
    final Mc mc;

    public ElytraBoosterModule() {
        super(ModuleTab.MOVEMENT, "Elytra Booster");
        this.mc = Mc.INSTANCE;
        register(TravelEvent.class, class324Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                Rotation currentRotation = RotationManager.INSTANCE.getCurrentRotation();
                int i = currentRotation.getYaw() > 0.0f ? 45 : -45;
                double dPow = 1.0d + (0.3d * Math.pow(Math.abs(((currentRotation.getYaw() + i) % 90.0f) - i) / 45.0f, 2.0d));
                boolean z = Math.abs(currentRotation.getPitch()) > 60.0f;
                Vec3d vec3d = class324Var.vec3d();
                class324Var.vec3d(new Vec3d(vec3d.x * dPow, z ? vec3d.y * dPow : vec3d.y, vec3d.z * dPow));
            }
        });
    }
}
