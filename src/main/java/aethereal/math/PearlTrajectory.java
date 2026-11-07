package aethereal.math;
import aethereal.model.TrajectoryPoint;

import java.util.List;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;

public final class PearlTrajectory {
    public final EnderPearlEntity pearl;
    public final List<TrajectoryPoint> steps;

    public PearlTrajectory(EnderPearlEntity enderPearlEntity, List<TrajectoryPoint> list) {
        this.pearl = enderPearlEntity;
        this.steps = list;
    }

        @Override
    public final String toString() {
        return getClass().getSimpleName() + "[" + "pearl=" + this.pearl + ", " + "steps=" + this.steps + "]";
    }
    @Override
    public final int hashCode() {
        return java.util.Objects.hash(this.pearl, this.steps);
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PearlTrajectory)) return false;
        PearlTrajectory o = (PearlTrajectory) obj;
        return java.util.Objects.equals(this.pearl, o.pearl) && java.util.Objects.equals(this.steps, o.steps);
    }
public EnderPearlEntity pearl() {
        return this.pearl;
    }

    public List<TrajectoryPoint> steps() {
        return this.steps;
    }
}
