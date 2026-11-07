package aethereal.module;
import aethereal.annotation.Aliases;
import aethereal.event.PacketReceiveEvent;
import aethereal.event.PlayerTickEvent;
import aethereal.event.WorldRenderEvent;
import aethereal.render.ColorStack;
import aethereal.type.BlockEspMethod;
import aethereal.type.BlockEspType;
import aethereal.type.Mc;
import aethereal.Lang;
import aethereal.ui.ModuleTab;
import aethereal.type.SettingUnit;
import aethereal.ui.setting.ColorSetting;
import aethereal.ui.setting.ModeSetting;
import aethereal.ui.setting.MultiSelectSetting;
import aethereal.ui.setting.NumberSetting;
import aethereal.Expensive;
import aethereal.render.ShapeRenderer;
import aethereal.util.StencilBufferUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

@Aliases(aliases = {"Block ESP", "Block Esp", "Ore ESP", "Ore Esp", "XRay Blocks"})
public class BlockESPModule extends Module {
    public final MultiSelectSetting<BlockEspType> typesSetting;
    public final ColorSetting colorSetting;
    public final NumberSetting maxDistance;
    public final ModeSetting<BlockEspMethod> methodSetting;
    public final Set<BlockPos> foundBlocks = new HashSet();
    public int tickCounter;
    public final Mc mc;

    public BlockESPModule() {
        super(ModuleTab.RENDER, "Block ESP");
        this.mc = Mc.INSTANCE;
        this.typesSetting = new MultiSelectSetting(Lang.BLOCK_ESP_TYPES, Lang.BLOCK_ESP_TYPES_DESC).values(BlockEspType.class);
        this.colorSetting = new ColorSetting(Lang.BLOCK_ESP_COLOR);
        this.maxDistance = new NumberSetting(Lang.BLOCK_ESP_MAX_DISTANCE, Lang.BLOCK_ESP_MAX_DISTANCE_DESC).currentValue(32.0f).range(8.0f, 64.0f).step(4.0f).unit(SettingUnit.BLOCKS);
        this.methodSetting = new ModeSetting(Lang.BLOCK_ESP_METHOD, Lang.BLOCK_ESP_METHOD_DESC).values(BlockEspMethod.class);
        addSettings(this.typesSetting, this.colorSetting, this.maxDistance, this.methodSetting);
        register(PlayerTickEvent.class, class130Var -> {
            if (isState() && class130Var.isPre() && this.mc.isWorldLoaded()) {
                int i = this.methodSetting.isSelected(BlockEspMethod.PACKET) ? 100 : 20;
                if (++this.tickCounter >= i) {
                    this.tickCounter = 0;
                    scan();
                }
            }
        });
        register(PacketReceiveEvent.class, class051Var -> {
            if (isState() && this.methodSetting.isSelected(BlockEspMethod.PACKET) && class051Var.getPacket() instanceof ChunkDeltaUpdateS2CPacket packet) {
                packet.visitUpdates((blockPos, blockState) -> {
                    if (matches(blockState)) {
                        if (this.isWithinDistance(blockPos)) {
                            this.foundBlocks.add(blockPos);
                        }
                    } else {
                        this.foundBlocks.remove(blockPos);
                    }
                });
            }
        });
        register(WorldRenderEvent.class, class016Var -> {
            if (isState() && this.mc.isWorldLoaded()) {
                ClientWorld world = (ClientWorld) this.mc.getWorld();
                MatrixStack matrixStack = class016Var.matrixStack();
                ColorStack class115VarColorStack = Expensive.INSTANCE.drawEngine().colorStack();
                int iGetRed = this.colorSetting.getRed();
                int iGetGreen = this.colorSetting.getGreen();
                int iGetBlue = this.colorSetting.getBlue();
                int iComputeColor = class115VarColorStack.computeColor(30, iGetRed, iGetGreen, iGetBlue);
                int iComputeColor2 = class115VarColorStack.computeColor(StencilBufferUtil.STENCIL_MASK, iGetRed, iGetGreen, iGetBlue);
                int iComputeColor3 = class115VarColorStack.computeColor(100, iGetRed, iGetGreen, iGetBlue);
                Iterator<BlockPos> it = this.foundBlocks.iterator();
                while (it.hasNext()) {
                    BlockPos blockPos = (BlockPos) it.next();
                    if (!matches(world.getBlockState(blockPos))) {
                        it.remove();
                    } else {
                        Box box = new Box(blockPos);
                        ShapeRenderer.INSTANCE.addBox(matrixStack.peek().getPositionMatrix(), box, iComputeColor);
                        ShapeRenderer.INSTANCE.addOutlineWireframe(matrixStack.peek().getPositionMatrix(), box, iComputeColor2, iComputeColor3, 3.0f);
                    }
                }
            }
        });
    }

    public boolean matches(BlockState blockState) {
        for (BlockEspType blockEspType : this.typesSetting.selectedValues()) {
            if (blockEspType.matches(blockState)) {
                return true;
            }
        }
        return false;
    }

    public boolean isWithinDistance(BlockPos blockPos) {
        double d = this.maxDistance.currentValue();
        BlockPos blockPosPlayer = this.mc.getPlayer().getBlockPos();
        return blockPos.getSquaredDistance(blockPosPlayer) <= d * d;
    }

    public void scan() {
        ClientPlayerEntity player = this.mc.getPlayer();
        ClientWorld world = (ClientWorld) this.mc.getWorld();
        if (player == null || world == null) {
            return;
        }
        double d = this.maxDistance.currentValue();
        double d2 = d * d;
        int iRadius = (int) Math.ceil(d);
        int iChunkRadius = (iRadius >> 4) + 1;
        int iChunkX = player.getBlockX() >> 4;
        int iChunkZ = player.getBlockZ() >> 4;
        HashSet hashSet = new HashSet();
        for (int i = -iChunkRadius; i <= iChunkRadius; i++) {
            for (int i2 = -iChunkRadius; i2 <= iChunkRadius; i2++) {
                WorldChunk worldChunk = world.getChunkManager().getWorldChunk(iChunkX + i, iChunkZ + i2);
                if (worldChunk == null || worldChunk.isEmpty()) {
                    continue;
                }
                int iBaseX = (iChunkX + i) << 4;
                int iBaseZ = (iChunkZ + i2) << 4;
                int iMinY = worldChunk.getBottomY();
                ChunkSection[] chunkSectionArr = worldChunk.getSectionArray();
                for (int i3 = 0; i3 < chunkSectionArr.length; i3++) {
                    ChunkSection chunkSection = chunkSectionArr[i3];
                    if (chunkSection == null || chunkSection.isEmpty()) {
                        continue;
                    }
                    int iBaseY = iMinY + (i3 << 4);
                    for (int i4 = 0; i4 < 16; i4++) {
                        for (int i5 = 0; i5 < 16; i5++) {
                            for (int i6 = 0; i6 < 16; i6++) {
                                BlockPos blockPos = new BlockPos(iBaseX + i4, iBaseY + i5, iBaseZ + i6);
                                if (blockPos.getSquaredDistance(player.getBlockPos()) > d2) {
                                    continue;
                                }
                                if (matches(world.getBlockState(blockPos))) {
                                    hashSet.add(blockPos);
                                }
                            }
                        }
                    }
                }
            }
        }
        this.foundBlocks.clear();
        this.foundBlocks.addAll(hashSet);
    }
}