package ru.expensive.mixin;

import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({Input.class})
public class InputMixin {

    @Shadow
    public float field_3905;

    @Shadow
    public float field_3907;
}
