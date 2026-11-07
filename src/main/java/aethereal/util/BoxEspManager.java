package aethereal.util;
import aethereal.model.BlockUpdateEntry;
import aethereal.event.BlockUpdateEvent;
import aethereal.internal.BlockUpdateTypeSwitchMap;
import aethereal.Expensive;
import aethereal.type.Mc;
import aethereal.event.PlayerInitEvent;
import aethereal.render.ShapeRenderer;
import aethereal.event.WorldRenderEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.joml.Matrix4f;

public class BoxEspManager {
    public static final BoxEspManager INSTANCE = new BoxEspManager();
    public static final int defaultColor = -16733441;
    public final Map<Block, Integer> blocks = new HashMap();
    public final Map<EntityType<?>, Integer> entities = new HashMap();
    public final Map<BlockPos, Integer> trackedPositions = new HashMap();

    public BoxEspManager() {
        Expensive.INSTANCE.eventDispatcher().register(BlockUpdateEvent.class, class189Var -> {
            switch (BlockUpdateTypeSwitchMap.switchMap[class189Var.type().ordinal()]) {
                case 1:
                    for (BlockUpdateEntry class190Var : class189Var.list()) {
                        Integer num = this.blocks.get(class190Var.state().getBlock());
                        if (num != null) {
                            this.trackedPositions.put(class190Var.pos(), Integer.valueOf(resolveColor(num.intValue())));
                        }
                    }
                    break;
                case 2:
                    for (BlockUpdateEntry class190Var2 : class189Var.list()) {
                        BlockPos blockPosPos = class190Var2.pos();
                        BlockState blockStateState = class190Var2.state();
                        this.trackedPositions.remove(blockPosPos);
                        Integer num2 = this.blocks.get(blockStateState.getBlock());
                        if (num2 != null) {
                            this.trackedPositions.put(blockPosPos, Integer.valueOf(resolveColor(num2.intValue())));
                        }
                    }
                    break;
                case 3:
                    Iterator<BlockUpdateEntry> it = class189Var.list().iterator();
                    while (it.hasNext()) {
                        this.trackedPositions.remove(it.next().pos());
                    }
                    break;
            }
        });
        Expensive.INSTANCE.eventDispatcher().register(PlayerInitEvent.class, class125Var -> {
            this.trackedPositions.clear();
        });
        Expensive.INSTANCE.eventDispatcher().register(WorldRenderEvent.class, class016Var -> {
            Mc class815Var = Mc.INSTANCE;
            if (class815Var.isWorldLoaded()) {
                if (this.trackedPositions.isEmpty() && this.entities.isEmpty()) {
                    return;
                }
                Matrix4f positionMatrix = class016Var.matrixStack().peek().getPositionMatrix();
                this.trackedPositions.forEach((blockPos, num) -> {
                    Box box = new Box(blockPos);
                    ShapeRenderer.INSTANCE.addBox(positionMatrix, box, withAlpha(num.intValue(), 30));
                    ShapeRenderer.INSTANCE.addOutlineWireframe(positionMatrix, box, num.intValue(), withAlpha(num.intValue(), 80), 1.5f);
                });
                if (this.entities.isEmpty()) {
                    return;
                }
                class815Var.getWorld().getEntitiesByClass(Entity.class, class815Var.getPlayer().getBoundingBox().expand(256.0d), entity -> {
                    return this.entities.containsKey(entity.getType());
                }).forEach(entity2 -> {
                    int iMethod001 = resolveColor(this.entities.get(entity2.getType()).intValue());
                    Box boundingBox = entity2.getBoundingBox();
                    ShapeRenderer.INSTANCE.addBox(positionMatrix, boundingBox, withAlpha(iMethod001, 30));
                    ShapeRenderer.INSTANCE.addOutlineWireframe(positionMatrix, boundingBox, iMethod001, withAlpha(iMethod001, 80), 1.5f);
                });
            }
        });
    }

    public Map<Block, Integer> getBlocks() {
        return this.blocks;
    }

    public Map<EntityType<?>, Integer> getEntities() {
        return this.entities;
    }

    public void clearAll() {
        this.blocks.clear();
        this.entities.clear();
        this.trackedPositions.clear();
    }

    public int resolveColor(int i) {
        return i == 0 ? defaultColor : i;
    }

    public static int withAlpha(int i, int i2) {
        return (i & 16777215) | (i2 << 24);
    }
}
