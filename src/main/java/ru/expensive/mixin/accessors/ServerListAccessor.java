package ru.expensive.mixin.accessors;

import java.util.List;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerList.class})
public interface ServerListAccessor {
    @Accessor("servers")
    List<ServerInfo> expensive$getServers();
}
