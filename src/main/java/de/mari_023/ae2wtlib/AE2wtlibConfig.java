package de.mari_023.ae2wtlib;

import appeng.platform.neoforge.common.ModConfigSpec;

/**
 * FABRIC: uses AE2 Fabricated's {@link ModConfigSpec} stand-in, which reads and writes the same TOML file
 * ({@code config/ae2wtlib.toml}). The NeoForge {@code define(name, double)} becomes an unbounded
 * {@code defineInRange}.
 */
public record AE2wtlibConfig(ModConfigSpec.DoubleValue magnetCardRangeValue) {
    public static final AE2wtlibConfig CONFIG;
    public static final ModConfigSpec SPEC;

    static {
        var builder = new ModConfigSpec.Builder();
        CONFIG = new AE2wtlibConfig(builder);
        SPEC = builder.build();
    }

    AE2wtlibConfig(ModConfigSpec.Builder builder) {
        this(builder.defineInRange("magnet_card_range", 16.0, 0.0, Double.MAX_VALUE));
    }

    public double magnetCardRange() {
        return magnetCardRangeValue().get();
    }
}
