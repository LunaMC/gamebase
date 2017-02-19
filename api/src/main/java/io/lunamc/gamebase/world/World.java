package io.lunamc.gamebase.world;

public interface World {

    WorldType getWorldType();

    Chunk requireChunk(int chunkX, int chunkZ);
}
