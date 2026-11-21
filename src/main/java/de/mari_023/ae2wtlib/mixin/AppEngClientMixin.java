package de.mari_023.ae2wtlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.client.AppEngClient;

import de.mari_023.ae2wtlib.AE2wtlibClient;

/**
 * FABRIC: client counterpart of {@link AppEngBaseMixin}.
 */
@Mixin(AppEngClient.class)
public abstract class AppEngClientMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2wtlib$initClient(CallbackInfo ci) {
        AE2wtlibClient.init();
    }
}
