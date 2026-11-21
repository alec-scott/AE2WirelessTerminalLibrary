package de.mari_023.ae2wtlib;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.items.tools.powered.powersink.PoweredItemCapabilities;
import appeng.platform.neoforge.capabilities.Capabilities;
import appeng.platform.neoforge.capabilities.RegisterCapabilitiesEvent;

import de.mari_023.ae2wtlib.api.AE2wtlibAPI;
import de.mari_023.ae2wtlib.api.AE2wtlibAPIEntrypoint;
import de.mari_023.ae2wtlib.api.terminal.ItemWT;
import de.mari_023.ae2wtlib.networking.*;

/**
 * FABRIC: replaces the NeoForge {@code @Mod} class {@code AE2wtlibForge}.
 * <p>
 * AE2 Fabricated registers its content from its client/dedicated-server entrypoints and offers no addon entrypoint.
 * Everything here needs AE2's registries, components and hotkey registry to exist, so {@link #init()} is called from
 * a mixin at the end of AE2's {@code AppEngBase} constructor (see {@code AppEngBaseMixin}), which makes the order
 * independent of entrypoint order. The steps follow the order of the NeoForge registry events.
 */
public final class AE2wtlibFabric {
    private static boolean initialized;

    private AE2wtlibFabric() {}

    public static void init() {
        if (initialized)
            return;
        initialized = true;

        new AE2wtlibAPIImplementation();
        AE2wtlibConfig.SPEC.load(FabricLoader.getInstance().getConfigDir().resolve(AE2wtlibAPI.MOD_NAME + ".toml"));
        AE2wtlibAdditionalComponents.init();

        // RegisterEvent(ITEM): items, then terminals (API: terminal definitions, upgrades, data components)
        AE2wtlibItems.DR.register();
        AE2wtlib.registerTerminals();
        AE2wtlibAPIEntrypoint.register();
        AE2wtlib.registerRecipes();
        AE2wtlib.registerHotkeyActions();
        AE2wtlibCreativeTab.init();
        // RegisterEvent(MENU)
        AE2wtlib.registerMenus();
        // FMLCommonSetupEvent
        AE2wtlib.registerGridLinkables();
        AE2wtlib.registerUpgrades();
        // BuildCreativeModeTabContentsEvent
        CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((tab, output) -> AE2wtlib.addToCreativeTab());

        registerPayloads();
        registerCapabilities();
        registerEvents();

        // Port testing: -Dae2wtlib.mixinAudit=true loads every mixin target once the server is up, so mixins on
        // classes that only load in game (e.g. on player join) fail at startup instead of later.
        if (Boolean.getBoolean("ae2wtlib.mixinAudit"))
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                MixinEnvironment.getCurrentEnvironment().audit();
                LOGGER.info("ae2wtlib mixin audit finished");
            });
    }

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AE2wtlibAPI.MOD_NAME);

    private static void registerPayloads() {
        registerC2S(CycleTerminalPacket.ID, CycleTerminalPacket.STREAM_CODEC);
        registerC2S(SelectTerminalPacket.ID, SelectTerminalPacket.STREAM_CODEC);
        registerC2S(TerminalSettingsPacket.ID, TerminalSettingsPacket.STREAM_CODEC);
        // client receivers are registered in AE2wtlibClient
        PayloadTypeRegistry.clientboundPlay().register(UpdateWUTPackage.ID, UpdateWUTPackage.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(UpdateRestockPacket.ID, UpdateRestockPacket.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(RestockAmountPacket.ID, RestockAmountPacket.STREAM_CODEC);
    }

    private static <T extends AE2wtlibPacket> void registerC2S(CustomPacketPayload.Type<T> id,
            StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        PayloadTypeRegistry.serverboundPlay().register(id, streamCodec);
        // Fabric runs play payload handlers on the server thread already (NeoForge: context.enqueueWork)
        ServerPlayNetworking.registerGlobalReceiver(id, (packet, context) -> packet.processPacketData(context.player()));
    }

    private static void registerCapabilities() {
        // RegisterCapabilitiesEvent: AE2 Fabricated's stand-in bridges Capabilities.Energy.ITEM to Team Reborn Energy
        var event = new RegisterCapabilitiesEvent();
        registerPowerStorageItem(event, AE2wtlibItems.UNIVERSAL_TERMINAL.asItem());
        registerPowerStorageItem(event, AE2wtlibItems.PATTERN_ACCESS_TERMINAL.asItem());
        registerPowerStorageItem(event, AE2wtlibItems.PATTERN_ENCODING_TERMINAL.asItem());
    }

    private static void registerPowerStorageItem(RegisterCapabilitiesEvent event, ItemWT item) {
        event.registerItem(Capabilities.Energy.ITEM,
                (_, context) -> new PoweredItemCapabilities(context, item, item), item);
    }

    /**
     * Game-bus events. Item pickup, finishing item use and bow nock/loose have no Fabric events and are mixins
     * ({@code ItemEntityMixin}, {@code LivingEntityMixin}, {@code BowItemMixin}).
     */
    private static void registerEvents() {
        // PlayerInteractEvent.RightClickBlock
        UseBlockCallback.EVENT.register((player, level, hand, hitResult) -> {
            if (player instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()) {
                var item = serverPlayer.getItemInHand(hand);
                AE2wtlibEvents.restock(serverPlayer, item, item, stack -> serverPlayer.setItemInHand(hand, stack));
            }
            return InteractionResult.PASS;
        });
        // PlayerInteractEvent.EntityInteractSpecific (the interactAt variant, which has a hit result)
        UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> {
            if (hitResult != null && player instanceof ServerPlayer serverPlayer && !serverPlayer.isSpectator()) {
                var item = serverPlayer.getItemInHand(hand);
                AE2wtlibEvents.restock(serverPlayer, item, item, stack -> serverPlayer.setItemInHand(hand, stack));
            }
            return InteractionResult.PASS;
        });
    }

    /** ArrowNockEvent / ArrowLooseEvent, called from {@code BowItemMixin}. */
    public static void onBowUse(Player player, ItemStack bow) {
        if (!(player instanceof ServerPlayer serverPlayer))
            return;
        ItemStack projectile = serverPlayer.getProjectile(bow);
        if (projectile.isEmpty())
            return;
        AE2wtlibEvents.restock(serverPlayer, projectile, projectile, (_) -> {
        });
    }
}
