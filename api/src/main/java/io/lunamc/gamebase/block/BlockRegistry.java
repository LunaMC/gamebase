package io.lunamc.gamebase.block;

import java.util.Optional;

public interface BlockRegistry {

    Optional<Block> getBlockByName(String name);

    Block register(String name, int paletteId);
}
