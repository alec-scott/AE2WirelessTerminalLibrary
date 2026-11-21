package de.mari_023.ae2wtlib;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.player.Player;

import appeng.platform.neoforge.network.codec.NeoForgeStreamCodecs;

import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.AE2wtlibComponents;
import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;
import de.mari_023.ae2wtlib.wct.magnet_card.MagnetMode;

public class AE2wtlibAdditionalComponents {
    public static final DataComponentType<MagnetMode> MAGNET_SETTINGS = AE2wtlibComponents.register("magnet_settings",
            builder -> builder.persistent(Codec.BYTE.comapFlatMap(b -> DataResult.success(MagnetMode.fromByte(b)),
                    MagnetMode::getId))
                    .networkSynchronized(NeoForgeStreamCodecs.enumCodec(MagnetMode.class)));

    /**
     * FABRIC: Fabric data attachment instead of a NeoForge attachment type. Like the NeoForge one it is neither
     * persistent nor synced and is not copied on respawn. Fabric initializers get no holder, so the handler is created
     * on first access in {@link #getCraftingTerminalHandler(Player)}.
     */
    public static final AttachmentType<CraftingTerminalHandler> CT_HANDLER = AttachmentRegistry
            .create(AE2wtlibAPI.id("ct_handler"));

    public static CraftingTerminalHandler getCraftingTerminalHandler(Player player) {
        var handler = player.getAttached(CT_HANDLER);
        if (handler == null) {
            handler = new CraftingTerminalHandler(player);
            player.setAttached(CT_HANDLER, handler);
        }
        return handler;
    }

    @SuppressWarnings("EmptyMethod")
    public static void init() {}
}
