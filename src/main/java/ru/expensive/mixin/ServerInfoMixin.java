package ru.expensive.mixin;

import aethereal.net.PinnableServer;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin({ServerInfo.class})
public class ServerInfoMixin implements PinnableServer {

    @Unique
    private boolean expensive$pinned;

    @Override
    public boolean expensive$isPinned() {
        return this.expensive$pinned;
    }

    @Override
    public void expensive$setPinned(boolean z) {
        this.expensive$pinned = z;
    }
}
