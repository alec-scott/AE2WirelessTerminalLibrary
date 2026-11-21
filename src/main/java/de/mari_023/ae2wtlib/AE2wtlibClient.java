package de.mari_023.ae2wtlib;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import appeng.platform.neoforge.client.event.RegisterMenuScreensEvent;
import appeng.platform.neoforge.client.network.ClientPacketDistributor;

import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.terminal.ItemWUT;
import de.mari_023.ae2wtlib.networking.*;
import de.mari_023.ae2wtlib.wct.CraftingTerminalHandler;

/**
 * FABRIC: replaces the client {@code @Mod} class. {@link #init()} is called from a mixin at the end of AE2's
 * {@code AppEngClient} constructor (after {@link AE2wtlibFabric#init()}, which runs at the end of its super
 * constructor). The NeoForge config screen is provided through Mod Menu and Cloth Config, when present.
 */
public class AE2wtlibClient {
    private static boolean initialized;

    public static void init() {
        if (initialized)
            return;
        initialized = true;

        AE2wtlibClientConfig.SPEC
                .load(FabricLoader.getInstance().getConfigDir().resolve(AE2wtlibAPI.MOD_NAME + "-client.toml"));
        AE2wtlib.registerScreens(new RegisterMenuScreensEvent());

        registerS2C(UpdateWUTPackage.ID);
        registerS2C(UpdateRestockPacket.ID);
        registerS2C(RestockAmountPacket.ID);

        // ClientTickEvent.Post
        ClientTickEvents.END_CLIENT_TICK.register(client -> clientTick());
    }

    private static <T extends AE2wtlibPacket> void registerS2C(CustomPacketPayload.Type<T> id) {
        // Fabric runs play payload handlers on the client thread already (NeoForge: context.enqueueWork)
        ClientPlayNetworking.registerGlobalReceiver(id, (packet, context) -> packet.processPacketData(context.player()));
    }

    public static void clientTick() {
        if (Minecraft.getInstance().player == null)
            return;
        CraftingTerminalHandler.getCraftingTerminalHandler(Minecraft.getInstance().player).checkTerminal();
    }

    /**
     * InputEvent.MouseScrollingEvent, called from {@code MouseHandlerMixin}.
     *
     * @return true to cancel the scroll
     */
    public static boolean mouseScroll(double scrollDeltaY) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.screen != null || !player.isShiftKeyDown() || scrollDeltaY == 0)
            return false;

        if (!(player.getMainHandItem().getItem() instanceof ItemWUT)
                && !(player.getOffhandItem().getItem() instanceof ItemWUT))
            return false;

        ClientPacketDistributor.sendToServer(new CycleTerminalPacket(scrollDeltaY < 0));
        return true;
    }
}
