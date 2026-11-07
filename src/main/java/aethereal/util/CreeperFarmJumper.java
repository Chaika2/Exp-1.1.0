package aethereal.util;
import aethereal.type.Mc;

import net.minecraft.client.network.ClientPlayerEntity;

public class CreeperFarmJumper {
    public final Mc mc = Mc.INSTANCE;
    public long lastJumpTime = 0;

    public void tick(ClientPlayerEntity clientPlayerEntity) {
        if (!clientPlayerEntity.isOnGround() || this.mc.getGameOptions().jumpKey.isPressed() || clientPlayerEntity.input.movementForward <= 0.0f || clientPlayerEntity.horizontalCollision) {
            return;
        }
        long time = this.mc.getWorld().getTime();
        if (time - this.lastJumpTime > 2) {
            this.lastJumpTime = time;
            clientPlayerEntity.jump();
        }
    }

    public void reset() {
        this.lastJumpTime = 0L;
    }
}
