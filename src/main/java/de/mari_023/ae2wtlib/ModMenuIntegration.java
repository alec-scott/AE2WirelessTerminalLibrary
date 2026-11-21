package de.mari_023.ae2wtlib;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import net.fabricmc.loader.api.FabricLoader;

/**
 * FABRIC: NeoForge's built-in {@code ConfigurationScreen} has no Fabric equivalent. The config screen is offered
 * through Mod Menu and built with Cloth Config, both optional.
 */
public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        if (!FabricLoader.getInstance().isModLoaded("cloth-config"))
            return parent -> null;
        return ClothConfigScreen::create;
    }
}
