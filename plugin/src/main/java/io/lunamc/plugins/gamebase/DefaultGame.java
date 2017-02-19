package io.lunamc.plugins.gamebase;

import io.lunamc.gamebase.Game;
import io.lunamc.plugins.gamebase.block.DefaultBlockRegistry;

import java.util.Objects;

public class DefaultGame implements Game {

    private final DefaultBlockRegistry blockRegistry = new DefaultBlockRegistry();
    private final MinecraftVersion version;

    public DefaultGame(MinecraftVersion version) {
        this.version = Objects.requireNonNull(version, "version must not be null");
    }

    public void initialize() {
        Initializer.initializeBlocks(this, version);
    }

    @Override
    public DefaultBlockRegistry getBlockRegistry() {
        return blockRegistry;
    }
}
