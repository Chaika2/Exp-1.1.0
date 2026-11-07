package ru.expensive.mixin.accessors;

import net.minecraft.client.gl.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({Framebuffer.class})
public interface FramebufferAccessor {
    @Accessor("depthAttachment")
    void setDepthAttachment(int i);

    @Accessor("fbo")
    int getFbo();

    @Accessor("colorAttachment")
    int getColorAttachment();
}
