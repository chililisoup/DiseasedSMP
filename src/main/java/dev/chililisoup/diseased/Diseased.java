package dev.chililisoup.diseased;

import dev.chililisoup.diseased.reg.ModCommands;
import dev.chililisoup.diseased.reg.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Diseased implements ModInitializer {
    public static final String MOD_ID = "diseased";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final float CRICK_AMOUNT = 0.0872665F;

    @Override
    public void onInitialize() {
        PlayerDiseases.noop();
        ModItems.noop();
        ModCommands.init();
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
