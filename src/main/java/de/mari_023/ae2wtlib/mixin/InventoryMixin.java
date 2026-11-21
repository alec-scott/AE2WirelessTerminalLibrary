package de.mari_023.ae2wtlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import de.mari_023.ae2wtlib.PickAction;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;

/**
 * FABRIC: NeoForge replaces the {@code isEnchanted()} check in {@code getSuitableHotbarSlot} with
 * {@code isNotReplaceableByPickAction}, so pick block never replaces a wireless terminal in the hotbar.
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    @Final
    public Player player;

    @WrapOperation(method = "getSuitableHotbarSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEnchanted()Z"))
    private boolean ae2wtlib$isNotReplaceableByPickAction(ItemStack stack, Operation<Boolean> original,
            @Local(index = 2) int index) {
        if (stack.getItem() instanceof ItemWT)
            return PickAction.isNotReplaceable(stack, player, index);
        return original.call(stack);
    }
}
