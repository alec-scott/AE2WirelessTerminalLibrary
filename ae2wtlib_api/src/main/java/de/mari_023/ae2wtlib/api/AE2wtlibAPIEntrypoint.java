package de.mari_023.ae2wtlib.api;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

import de.mari_023.ae2wtlib.api.registration.AddTerminalEvent;
import de.mari_023.ae2wtlib.api.registration.UpgradeHelper;

/**
 * FABRIC: on NeoForge this ran from the API mod's own {@code RegisterEvent} listener for the item registry. Fabric
 * has no registry events, and AE2 Fabricated has no addon entrypoint, so ae2wtlib calls {@link #register()} from its
 * hook at the end of AE2's initialization, after its terminal handlers are added.
 */
public final class AE2wtlibAPIEntrypoint {
    private AE2wtlibAPIEntrypoint() {}

    public static void register() {
        AddTerminalEvent.run();
        UpgradeHelper.addUpgrades();
        for (var entry : AE2wtlibComponents.DR.entrySet())
            Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, entry.getKey(), entry.getValue());
    }
}
