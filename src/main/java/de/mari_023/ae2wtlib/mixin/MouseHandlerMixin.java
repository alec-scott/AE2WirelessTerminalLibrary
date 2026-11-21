package de.mari_023.ae2wtlib.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import de.mari_023.ae2wtlib.AE2wtlibClient;

/**
 * FABRIC: NeoForge's InputEvent.MouseScrollingEvent, which fires for scrolling without a screen open, with the delta
 * after the discrete-scroll and sensitivity options are applied.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void ae2wtlib$cycleTerminal(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (window != minecraft.getWindow().handle() || minecraft.screen != null || minecraft.getOverlay() != null
                || minecraft.player == null)
            return;
        boolean discrete = minecraft.options.discreteMouseScroll().get();
        double sensitivity = minecraft.options.mouseWheelSensitivity().get();
        double deltaY = (discrete ? Math.signum(yOffset) : yOffset) * sensitivity;
        if (AE2wtlibClient.mouseScroll(deltaY))
            ci.cancel();
    }
}
