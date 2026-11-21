package de.mari_023.ae2wtlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import appeng.core.AppEngBase;

import de.mari_023.ae2wtlib.AE2wtlibFabric;

/**
 * FABRIC: AE2 Fabricated registers everything in this constructor (called from its client or dedicated server
 * entrypoint) and has no addon entrypoint. ae2wtlib's registration needs AE2's to be complete, so it runs here.
 */
@Mixin(AppEngBase.class)
public abstract class AppEngBaseMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void ae2wtlib$init(CallbackInfo ci) {
        AE2wtlibFabric.init();
    }
}
