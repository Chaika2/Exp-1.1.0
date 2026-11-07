package aethereal.type;
import aethereal.module.CreeperFarmModule;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class CreeperFarmPatrolPhase {
    public final CreeperFarmModule module;
    public final List<BlockPos> patrolPoints = new ArrayList();
    public int currentIndex = 0;
    public BlockPos currentTarget = null;

    public void tickPatrolPhase(ClientPlayerEntity clientPlayerEntity) {
        if (this.patrolPoints.isEmpty()) {
            buildPatrolPoints();
        }
        if (!this.module.getCreepersSortedByDistance().isEmpty()) {
            this.module.getPhaseManager().setPhase(TpLootStage.APPROACH);
            this.currentTarget = null;
            return;
        }
        if (this.currentIndex >= this.patrolPoints.size()) {
            this.currentIndex = 0;
            return;
        }
        BlockPos blockPos = this.patrolPoints.get(this.currentIndex);
        if (this.currentTarget != null) {
        }
        if (this.currentTarget == null) {
            this.currentTarget = blockPos;
            return;
        }
        if (clientPlayerEntity.getBlockPos().isWithinDistance(this.currentTarget, 5.0d)) {
            this.currentIndex++;
            if (this.currentIndex < this.patrolPoints.size()) {
                this.currentTarget = this.patrolPoints.get(this.currentIndex);
            } else {
                this.module.getPhaseManager().setPhase(TpLootStage.APPROACH);
                this.currentTarget = null;
            }
        }
    }

    public void buildPatrolPoints() {
        this.patrolPoints.clear();
        BlockPos regionMin = this.module.getRegionMin();
        BlockPos regionMax = this.module.getRegionMax();
        int x = regionMin.getX() >> 4;
        int x2 = regionMax.getX() >> 4;
        int z = regionMin.getZ() >> 4;
        int z2 = regionMax.getZ() >> 4;
        for (int i = x; i <= x2; i++) {
            for (int i2 = z; i2 <= z2; i2++) {
                this.patrolPoints.add(new BlockPos(Math.max(regionMin.getX(), Math.min((i << 4) + 8, regionMax.getX())), regionMin.getY(), Math.max(regionMin.getZ(), Math.min((i2 << 4) + 8, regionMax.getZ()))));
            }
        }
    }

    public void reset() {
        this.currentIndex = 0;
        this.currentTarget = null;
        this.patrolPoints.clear();
    }

    public CreeperFarmPatrolPhase(CreeperFarmModule class597Var) {
        this.module = class597Var;
    }
}
