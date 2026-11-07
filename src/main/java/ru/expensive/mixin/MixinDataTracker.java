package ru.expensive.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.util.math.EulerAngle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({DataTracker.class})
public abstract class MixinDataTracker {
    @Inject(method = {"set(Lnet/minecraft/entity/data/TrackedData;Ljava/lang/Object;)V"}, at = {@At("HEAD")}, cancellable = true)
    private <T> void onSet(TrackedData<T> trackedData, T t, CallbackInfo callbackInfo) {
        TrackedDataHandler trackedDataHandlerDataType = trackedData.dataType();
        if (trackedDataHandlerDataType == TrackedDataHandlerRegistry.BYTE && !(t instanceof Byte)) {
            callbackInfo.cancel();
        }
        if (trackedDataHandlerDataType != TrackedDataHandlerRegistry.ROTATION || (t instanceof EulerAngle)) {
            return;
        }
        callbackInfo.cancel();
    }
}
