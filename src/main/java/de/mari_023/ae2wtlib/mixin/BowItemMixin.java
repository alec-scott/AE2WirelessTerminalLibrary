package de.mari_023.ae2wtlib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import de.mari_023.ae2wtlib.AE2wtlibFabric;

/**
 * FABRIC: NeoForge's ArrowNockEvent (start of {@code BowItem.use}) and ArrowLooseEvent (start of
 * {@code BowItem.releaseUsing}). Both restock the arrow stack the bow would use.
 */
@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "use", at = @At("HEAD"))
    private void ae2wtlib$restockOnNock(Level level, Player player, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        AE2wtlibFabric.onBowUse(player, player.getItemInHand(hand));
    }

    @Inject(method = "releaseUsing", at = @At("HEAD"))
    private void ae2wtlib$restockOnLoose(ItemStack stack, Level level, LivingEntity entity, int timeLeft,
            CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player)
            AE2wtlibFabric.onBowUse(player, stack);
    }
}
