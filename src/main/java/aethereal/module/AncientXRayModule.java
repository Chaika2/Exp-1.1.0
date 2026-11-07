package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.ui.setting.ColorSetting;
import aethereal.Expensive;
import aethereal.Lang;
import aethereal.type.Mc;
import aethereal.ui.ModuleTab;
import aethereal.type.NotificationType;
import aethereal.event.PacketReceiveEvent;
import aethereal.render.ShapeRenderer;
import aethereal.event.WorldRenderEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

@Aliases(aliases = {"X Ray", "Funtime", "Ancient XRay"})
public class AncientXRayModule extends Module {
    public final ColorSetting colorSetting;
    public final List<BlockPos> foundBlocks;

    public AncientXRayModule() {
        super(ModuleTab.MISC, "Ancient XRay");
        this.colorSetting = new ColorSetting(Lang.ANCIENT_XRAY_COLOR);
        this.foundBlocks = new ArrayList();
        addSettings(this.colorSetting);
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState()) {
                if ((class051Var.getPacket()) instanceof ChunkDeltaUpdateS2CPacket packet ) {
                    packet.visitUpdates((blockPos, blockState) -> {
                        Block block = blockState.getBlock();
                        BlockPos blockPosAdd = blockPos.add(0, 0, 0);
                        if (block != Blocks.ANCIENT_DEBRIS || this.foundBlocks.contains(blockPosAdd)) {
                            return;
                        }
                        Expensive.INSTANCE.notificationRepository().post(NotificationType.INFO, (Text) Text.literal(Lang.ANCIENTXRAY_FOUND.effective().replace("{pos}", blockPosAdd.toShortString())), 3L, TimeUnit.SECONDS);
                        this.foundBlocks.add(blockPosAdd);
                    });
                }
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState()) {
                try {
                    ClientWorld world = Mc.INSTANCE.getWorld();
                    MatrixStack matrixStack = class016Var.matrixStack();
                    for (BlockPos blockPos : this.foundBlocks) {
                        if (world.getBlockState(blockPos).getBlock() != Blocks.ANCIENT_DEBRIS) {
                            this.foundBlocks.remove(blockPos);
                        } else {
                            ShapeRenderer.INSTANCE.addOutline(matrixStack.peek().getPositionMatrix(), new Box(blockPos), this.colorSetting.getColor(), 1.0f);
                        }
                    }
                } catch (Exception e) {
                    Expensive.LOGGER.error("Ancient XRay", e);
                }
            }
        });
    }
}
