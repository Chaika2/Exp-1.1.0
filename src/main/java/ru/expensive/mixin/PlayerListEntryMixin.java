package ru.expensive.mixin;

import aethereal.type.Mc;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({PlayerListEntry.class})
public class PlayerListEntryMixin {

    @Shadow
    @Final
    private GameProfile field_3741;

    @Unique
    private Identifier capeTexture = Identifier.of("expensive", "textures/cape.png");

    @Inject(method = {"getSkinTextures"}, at = {@At("RETURN")}, cancellable = true)
    private void injectCapeCosmetic(CallbackInfoReturnable<SkinTextures> callbackInfoReturnable) {
        if (this.capeTexture == null || this.field_3741 == null || !this.field_3741.getName().equals(Mc.INSTANCE.getSession().getUsername())) {
            return;
        }
        SkinTextures skinTextures = (SkinTextures) callbackInfoReturnable.getReturnValue();
        callbackInfoReturnable.setReturnValue(new SkinTextures(skinTextures.texture(), skinTextures.textureUrl(), this.capeTexture, skinTextures.elytraTexture(), skinTextures.model(), skinTextures.secure()));
    }
}
