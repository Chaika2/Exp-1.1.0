package aethereal.util;
import aethereal.event.ChunkLoadEvent;
import aethereal.model.DummyLivingEntity;
import aethereal.type.EntityLifecycleAction;
import aethereal.event.EntityLifecycleEvent;
import aethereal.type.EventPriority;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.PlayerInitEvent;
import aethereal.model.PlayerSnapshot;
import aethereal.event.PlayerTickEvent;
import aethereal.module.ProjectilePredictionModule;
import aethereal.math.ProjectileTrajectory;
import aethereal.model.TrajectoryPoint;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import net.minecraft.util.math.ChunkPos;

public class PlayerSnapshotManager {
    public static final PlayerSnapshotManager INSTANCE = new PlayerSnapshotManager();
    public final List<ProjectileTrajectory> projectiles = new ArrayList();
    public final Mc wrapper = Mc.INSTANCE;
    public final int maxTicks = 40;
    public final Map<Integer, ArrayDeque<PlayerSnapshot>> historyById = new HashMap();

    public PlayerSnapshotManager() {
        Expensive.INSTANCE.eventDispatcher().register(PlayerInitEvent.class, class125Var -> {
            this.historyById.clear();
        });
        Expensive.INSTANCE.eventDispatcher().register(ChunkLoadEvent.class, class334Var -> {
            ChunkPos chunkPos = class334Var.chunkPos();
            this.projectiles.stream().filter(class371Var -> {
                return class371Var.chunks.contains(chunkPos);
            }).forEach(class371Var2 -> {
                ProjectilePredictionModule class564Var = (ProjectilePredictionModule) Expensive.INSTANCE.moduleRepository().get(ProjectilePredictionModule.class);
                TrajectoryPoint class372Var = (TrajectoryPoint) class371Var2.steps.getFirst();
                Entity entity = class371Var2.entity;
                Pair<List<ChunkPos>, List<TrajectoryPoint>> pairPredictEntity = class564Var.predictEntity(entity, class372Var.velocity, class372Var.position, entity instanceof ThrownItemEntity);
                class371Var2.chunks.clear();
                class371Var2.steps.clear();
                class371Var2.chunks.addAll((Collection) pairPredictEntity.getLeft());
                class371Var2.steps.addAll((Collection) pairPredictEntity.getRight());
            });
        });
        Expensive.INSTANCE.eventDispatcher().register(EntityLifecycleEvent.class, class331Var -> {
            ProjectilePredictionModule class564Var = (ProjectilePredictionModule) Expensive.INSTANCE.moduleRepository().get(ProjectilePredictionModule.class);
            Entity thrownItemEntityEntity = class331Var.entity();
            if (thrownItemEntityEntity == Mc.INSTANCE.getPlayer() || Objects.requireNonNull(class331Var.type()) != EntityLifecycleAction.ADD) {
                return;
            }
            Objects.requireNonNull(thrownItemEntityEntity);
            if (thrownItemEntityEntity instanceof ThrownItemEntity) {
                ThrownItemEntity thrownItemEntity = (ThrownItemEntity) thrownItemEntityEntity;
                registerProjectile(thrownItemEntity, thrownItemEntity.getStack(), class564Var.predictEntity(thrownItemEntity, thrownItemEntity.getVelocity(), thrownItemEntity.getPos(), true));
            } else if (thrownItemEntityEntity instanceof PersistentProjectileEntity) {
                PersistentProjectileEntity persistentProjectileEntity = (PersistentProjectileEntity) thrownItemEntityEntity;
                registerProjectile(persistentProjectileEntity, persistentProjectileEntity.getItemStack(), class564Var.predictEntity(persistentProjectileEntity, persistentProjectileEntity.getVelocity(), persistentProjectileEntity.getPos(), false));
            }
        });
        Expensive.INSTANCE.eventDispatcher().register(PlayerTickEvent.class, class130Var -> {
            ClientWorld world;
            if (class130Var.isPre() && (world = this.wrapper.getWorld()) != null) {
                this.projectiles.removeIf((v0) -> {
                    return v0.tick();
                });
                HashSet hashSet = new HashSet(512);
                for (Entity livingEntity : world.getEntities()) {
                    if (livingEntity instanceof LivingEntity) {
                        LivingEntity livingEntity2 = (LivingEntity) livingEntity;
                        int id = livingEntity2.getId();
                        hashSet.add(Integer.valueOf(id));
                        ArrayDeque<PlayerSnapshot> arrayDequeComputeIfAbsent = this.historyById.computeIfAbsent(Integer.valueOf(id), num -> {
                            return new ArrayDeque(41);
                        });
                        arrayDequeComputeIfAbsent.addFirst(PlayerSnapshot.from(livingEntity2));
                        while (arrayDequeComputeIfAbsent.size() > 41) {
                            arrayDequeComputeIfAbsent.removeLast();
                        }
                    }
                }
                this.historyById.keySet().removeIf(num2 -> {
                    return !hashSet.contains(num2);
                });
            }
        }, EventPriority.HIGHEST);
    }

    public LivingEntity createEntity(EntityType<?> entityType) {
        return new DummyLivingEntity(this, entityType, this.wrapper.getWorld());
    }

    public Optional<PlayerSnapshot> getSnapshot(LivingEntity livingEntity, int i) {
        ArrayDeque<PlayerSnapshot> arrayDeque = this.historyById.get(Integer.valueOf(livingEntity.getId()));
        if (arrayDeque == null) {
            return Optional.empty();
        }
        if (i < 0 || i > 40) {
            return Optional.empty();
        }
        int i2 = 0;
        for (PlayerSnapshot class373Var : arrayDeque) {
            int i3 = i2;
            i2++;
            if (i3 == i) {
                return Optional.of(class373Var);
            }
        }
        return Optional.empty();
    }

    public void registerProjectile(Entity entity, ItemStack itemStack, Pair<List<ChunkPos>, List<TrajectoryPoint>> pair) {
        this.projectiles.removeIf(class371Var -> {
            return class371Var.entity.getUuid().equals(entity.getUuid());
        });
        this.projectiles.add(new ProjectileTrajectory(entity, itemStack, (List) pair.getLeft(), (List) pair.getRight()));
    }

    public PlayerSnapshot getSnapshotOrDefault(LivingEntity livingEntity, int i) {
        return getSnapshot(livingEntity, i).orElse(PlayerSnapshot.from(livingEntity));
    }

    public Stream<PlayerSnapshot> getSnapshots(LivingEntity livingEntity, int i) {
        ArrayDeque<PlayerSnapshot> arrayDeque = this.historyById.get(Integer.valueOf(livingEntity.getId()));
        if (arrayDeque == null) {
            return Stream.empty();
        }
        return arrayDeque.stream().limit(Math.min(Math.max(i, 0), 40) + 1);
    }

    public List<ProjectileTrajectory> getProjectiles() {
        return this.projectiles;
    }

    public Mc getWrapper() {
        return this.wrapper;
    }

    public int getMAX_TICKS() {
        Objects.requireNonNull(this);
        return 40;
    }

    public Map<Integer, ArrayDeque<PlayerSnapshot>> getHistoryById() {
        return this.historyById;
    }
}
