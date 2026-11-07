package ru.expensive.mixin;

import aethereal.Expensive;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({PlayerInventory.class})
public class PlayerInventoryMixin {

    @Shadow
    public int field_7545;

    @ModifyExpressionValue(method = {"dropSelectedItem", "getBlockBreakingSpeed", "getMainHandStack"}, at = {@At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerInventory;selectedSlot:I")})
    private int hookOverrideOriginalSlot(int i) {
        return ((PlayerInventory) (Object) this).player == MinecraftClient.getInstance().player ? Expensive.INSTANCE.inventoryService().hotbarSlotSwapper().getServersideSlot() : i;
    }
}
