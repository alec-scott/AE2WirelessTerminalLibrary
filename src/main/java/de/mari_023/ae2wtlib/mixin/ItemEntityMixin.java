package de.mari_023.ae2wtlib.mixin;

import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import de.mari_023.ae2wtlib.AE2wtlibEvents;

/**
 * FABRIC: NeoForge's ItemEntityPickupEvent.Pre, fired at the start of {@code playerTouch}. With the default pickup
 * result the item is only inserted when vanilla would allow the pickup (no pickup delay, no other target player).
 * Whatever is not inserted into the ME system is then picked up by vanilla as usual.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow
    private @Nullable UUID target;

    @Shadow
    public abstract boolean hasPickUpDelay();

    @Inject(method = "playerTouch", at = @At("HEAD"))
    private void ae2wtlib$pickupToME(Player player, CallbackInfo ci) {
        if (hasPickUpDelay())
            return;
        if (target != null && !target.equals(player.getUUID()))
            return;
        AE2wtlibEvents.insertStackInME((ItemEntity) (Object) this, player);
    }
}
