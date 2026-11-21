package de.mari_023.ae2wtlib;

import appeng.platform.neoforge.common.ModConfigSpec;

/**
 * FABRIC: uses AE2 Fabricated's {@link ModConfigSpec} stand-in ({@code config/ae2wtlib-client.toml}).
 */
public record AE2wtlibClientConfig(ModConfigSpec.BooleanValue alwaysShowTerminalSelectorValue) {
    public static final AE2wtlibClientConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        var builder = new ModConfigSpec.Builder();
        CONFIG = new AE2wtlibClientConfig(builder);
        SPEC = builder.build();
    }

    AE2wtlibClientConfig(ModConfigSpec.Builder builder) {
        this(builder.define("always_show_terminal_selector", false));
    }

    public boolean alwaysShowTerminalSelector() {
        return alwaysShowTerminalSelectorValue().get();
    }
}
