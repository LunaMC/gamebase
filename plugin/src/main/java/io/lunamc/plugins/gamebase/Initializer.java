package io.lunamc.plugins.gamebase;

import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.block.BlockRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Initializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Initializer.class);

    private Initializer() {
    }

    public static void initializeBlocks(Game game, MinecraftVersion version) {
        BlockRegistry blockRegistry = game.getBlockRegistry();
        boolean handled = false;
        switch (version) {
            case VERSION_1_11_2:
                handled = true;
            default:
                if (!handled)
                    LOGGER.warn("Unknown game version " + version + ". Game instance will be initialized with default blocks.");
                registerBlock(blockRegistry, "minecraft:stone", 0x01);
                registerBlock(blockRegistry, "minecraft:grass", 0x02);
                registerBlock(blockRegistry, "minecraft:dirt", 0x03);
                registerBlock(blockRegistry, "minecraft:cobblestone", 0x04);
                registerBlock(blockRegistry, "minecraft:planks", 0x05);
                registerBlock(blockRegistry, "minecraft:sapling", 0x06);
                registerBlock(blockRegistry, "minecraft:bedrock", 0x07);
        }
    }

    private static void registerBlock(BlockRegistry blockRegistry, String name, int paletteId) {
        try {
            blockRegistry.register(name, paletteId);
        } catch (Throwable throwable) {
            LOGGER.warn("An error occurred while registering " + name + ": " + throwable.getMessage());
        }
    }
}
