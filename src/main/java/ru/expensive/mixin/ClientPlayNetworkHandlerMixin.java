package ru.expensive.mixin;

import aethereal.event.TabListEntryEvent;
import aethereal.util.ScoreboardHelper;
import aethereal.Expensive;
import aethereal.event.BlockUpdateEvent;
import aethereal.model.BlockUpdateEntry;
import aethereal.type.BlockUpdateType;
import aethereal.event.RenderOverlayEvent;
import aethereal.type.RenderOverlayType;
import aethereal.event.ChunkLoadEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListHeaderS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ClientPlayNetworkHandler.class})
public abstract class ClientPlayNetworkHandlerMixin {

    @Shadow
    private ClientWorld field_3699;

    @Inject(method = {"handlePlayerListAction"}, at = {@At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z")})
    private void playerListActionHook(PlayerListS2CPacket.Action action, PlayerListS2CPacket.Entry entry, PlayerListEntry playerListEntry, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new TabListEntryEvent(playerListEntry));
    }

    @Redirect(method = {"onEntityStatus"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;showFloatingItem(Lnet/minecraft/item/ItemStack;)V"))
    private void onEntityStatus(GameRenderer gameRenderer, ItemStack itemStack) {
        RenderOverlayEvent class252Var = new RenderOverlayEvent(RenderOverlayType.TOTEM_POP);
        Expensive.INSTANCE.eventDispatcher().dispatch(class252Var);
        if (class252Var.isCancelled()) {
            return;
        }
        gameRenderer.showFloatingItem(itemStack);
    }

    @Inject(method = {"onChunkData"}, at = {@At("RETURN")})
    private void onChunkDataHook(ChunkDataS2CPacket chunkDataS2CPacket, CallbackInfo callbackInfo) {
        Expensive.INSTANCE.eventDispatcher().dispatch(new ChunkLoadEvent(new ChunkPos(chunkDataS2CPacket.getChunkX(), chunkDataS2CPacket.getChunkZ())));
        scanChunk(chunkDataS2CPacket.getLightData().getBlockNibbles(), this.field_3699.getChunk(chunkDataS2CPacket.getChunkX(), chunkDataS2CPacket.getChunkZ()), BlockUpdateType.LOAD);
    }

    @Inject(method = {"onUnloadChunk"}, at = {@At("HEAD")})
    private void onUnloadChunkHook(UnloadChunkS2CPacket unloadChunkS2CPacket, CallbackInfo callbackInfo) {
        scanChunk(new ArrayList(), this.field_3699.getChunk(unloadChunkS2CPacket.pos().x, unloadChunkS2CPacket.pos().z), BlockUpdateType.UNLOAD);
    }

    @Inject(method = {"onPlayerListHeader"}, at = {@At("HEAD")})
    public void onPlayerListHeader(PlayerListHeaderS2CPacket playerListHeaderS2CPacket, CallbackInfo callbackInfo) {
        Text textHeader = playerListHeaderS2CPacket.header();
        if (textHeader != null) {
            ScoreboardHelper.INSTANCE.setHeader(textHeader);
        }
    }

    @Unique
    private void scanChunk(List<byte[]> list, WorldChunk worldChunk, BlockUpdateType class191Var) {
        int startX = worldChunk.getPos().getStartX();
        int startZ = worldChunk.getPos().getStartZ();
        ArrayList arrayList = new ArrayList();
        byte[] bArr = list.isEmpty() ? null : (byte[]) list.getFirst();
        ChunkNibbleArray chunkNibbleArray = bArr == null ? null : new ChunkNibbleArray(Arrays.copyOf(bArr, bArr.length));
        for (int i = 0; i <= worldChunk.getHighestNonEmptySection(); i++) {
            int i2 = i;
            arrayList.add(CompletableFuture.runAsync(() -> {
                ArrayList arrayList2 = new ArrayList();
                ChunkSection section = worldChunk.getSection(i2);
                int bottomY = (i2 + (worldChunk.getBottomY() >> 4)) << 4;
                for (int i3 = 0; i3 < 16; i3++) {
                    for (int i4 = 0; i4 < 16; i4++) {
                        for (int i5 = 0; i5 < 16; i5++) {
                            arrayList2.add(new BlockUpdateEntry(new BlockPos(startX | i3, bottomY | i5, startZ | i4), class191Var.equals(BlockUpdateType.LOAD) ? section.getBlockState(i3, i5, i4) : Blocks.AIR.getDefaultState(), chunkNibbleArray == null ? 0 : chunkNibbleArray.get(i3, i5, i4)));
                        }
                    }
                }
                if (arrayList2.isEmpty()) {
                    return;
                }
                Expensive.INSTANCE.eventDispatcher().dispatch(new BlockUpdateEvent(arrayList2, class191Var));
            }));
        }
        CompletableFuture.allOf((CompletableFuture[]) arrayList.toArray(new CompletableFuture[0])).join();
    }
}
