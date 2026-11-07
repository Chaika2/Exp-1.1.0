package aethereal.math;
import aethereal.model.TrajectoryPoint;

import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.ChunkPos;

public class ProjectileTrajectory {
    public final List<ChunkPos> chunks;
    public final Entity entity;
    public final ItemStack stack;
    public final List<TrajectoryPoint> steps;

    public ProjectileTrajectory(Entity entity, ItemStack itemStack, List list, List list2) {
        this.entity = entity;
        this.stack = itemStack;
        this.chunks = list;
        this.steps = list2;
    }

    public Entity entity() {
        return this.entity;
    }

    public ItemStack stack() {
        return this.stack;
    }

    public List steps() {
        return this.steps;
    }

    public boolean tick() {
        return this.entity.isRemoved();
    }
}
