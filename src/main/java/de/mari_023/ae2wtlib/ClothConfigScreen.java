package de.mari_023.ae2wtlib;

import me.shedaniel.clothconfig2.api.ConfigBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import de.mari_023.ae2wtlib.api.AE2wtlibAPI;

/**
 * FABRIC: replacement for NeoForge's generated config screen. Same entries and translation keys
 * ({@code ae2wtlib.configuration.*}).
 */
final class ClothConfigScreen {
    private ClothConfigScreen() {}

    static Screen create(Screen parent) {
        var builder = ConfigBuilder.create().setParentScreen(parent)
                .setTitle(Component.literal("AE2WTLib"));
        var entries = builder.entryBuilder();

        var client = builder.getOrCreateCategory(Component.translatableWithFallback("ae2wtlib.configuration.section.client", "Client"));
        var alwaysShow = AE2wtlibClientConfig.CONFIG.alwaysShowTerminalSelectorValue();
        client.addEntry(entries
                .startBooleanToggle(Component.translatable(key("always_show_terminal_selector")), alwaysShow.get())
                .setDefaultValue(alwaysShow.getDefault())
                .setSaveConsumer(alwaysShow::set)
                .build());

        var common = builder.getOrCreateCategory(Component.translatableWithFallback("ae2wtlib.configuration.section.common", "Common"));
        var magnetRange = AE2wtlibConfig.CONFIG.magnetCardRangeValue();
        common.addEntry(entries
                .startDoubleField(Component.translatable(key("magnet_card_range")), magnetRange.get())
                .setDefaultValue(magnetRange.getDefault())
                .setMin(0)
                .setSaveConsumer(magnetRange::set)
                .build());

        builder.setSavingRunnable(() -> {
            AE2wtlibClientConfig.SPEC.save();
            AE2wtlibConfig.SPEC.save();
        });
        return builder.build();
    }

    private static String key(String name) {
        return AE2wtlibAPI.MOD_NAME + ".configuration." + name;
    }
}
