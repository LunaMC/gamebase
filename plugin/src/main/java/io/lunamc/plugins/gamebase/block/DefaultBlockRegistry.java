package io.lunamc.plugins.gamebase.block;

import io.lunamc.gamebase.block.Block;
import io.lunamc.gamebase.block.BlockRegistry;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultBlockRegistry implements BlockRegistry {

    private final ConcurrentMap<String, DefaultBlock> blocks = new ConcurrentHashMap<>();

    public DefaultBlockRegistry() {
        putBlock(DefaultBlock.AIR);
    }

    @Override
    public Optional<Block> getBlockByName(String name) {
        return Optional.ofNullable(blocks.get(name));
    }

    @Override
    public Block register(String name, int paletteId) {
        return putBlock(new DefaultBlock(name, paletteId));
    }

    private Block putBlock(DefaultBlock block)  {
        String name = block.getName();
        DefaultBlock previous = blocks.putIfAbsent(name, block);
        if (previous != null && !Objects.equals(block, previous))
            throw new UnsupportedOperationException("A block identified by \"" + name + "\" is already registered with a different specification");
        return previous != null ? previous : block;
    }
}
