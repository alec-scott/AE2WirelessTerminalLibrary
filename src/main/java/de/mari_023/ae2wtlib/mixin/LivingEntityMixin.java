package de.mari_023.ae2wtlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import de.mari_023.ae2wtlib.AE2wtlibEvents;

/**
 * FABRIC: NeoForge's LivingEntityUseItemEvent.Finish, fired with a copy of the used stack and the stack returned by
 * {@code finishUsingItem}, which the handler may replace.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapOperation(method = "completeUsingItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack ae2wtlib$restockAfterUse(ItemStack useItem, Level level, LivingEntity entity,
            Operation<ItemStack> original) {
        if (!((Object) this instanceof ServerPlayer player))
            return original.call(useItem, level, entity);
        ItemStack copy = useItem.copy();
        ItemStack result = original.call(useItem, level, entity);
        ItemStack[] finalResult = { result };
        AE2wtlibEvents.restock(player, copy, result, stack -> finalResult[0] = stack);
        return finalResult[0];
    }
}
