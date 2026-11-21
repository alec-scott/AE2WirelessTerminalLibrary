package de.mari_023.ae2wtlib;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import de.mari_023.ae2wtlib.api.terminal.ItemWT;

/**
 * FABRIC: dispatch for NeoForge's {@code IItemExtension#isNotReplaceableByPickAction}. Wireless terminals override it
 * ({@link ItemWT#isNotReplaceableByPickAction}); every other item keeps NeoForge's default, which is vanilla's
 * {@code isEnchanted()} check.
 */
public final class PickAction {
    private PickAction() {}

    public static boolean isNotReplaceable(ItemStack stack, Player player, int inventorySlot) {
        if (stack.getItem() instanceof ItemWT terminal)
            return terminal.isNotReplaceableByPickAction(stack, player, inventorySlot);
        return stack.isEnchanted();
    }
}
