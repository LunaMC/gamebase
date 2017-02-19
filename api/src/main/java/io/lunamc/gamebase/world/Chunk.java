package io.lunamc.gamebase.world;

import io.lunamc.common.network.Connection;
import io.lunamc.gamebase.block.Block;

public interface Chunk {

    int CHUNK_DIMENSION = 16;
    int BLOCKS_PER_CHUNK = CHUNK_DIMENSION * CHUNK_DIMENSION * CHUNK_DIMENSION;
    int HALF_BLOCKS_PER_CHUNK = BLOCKS_PER_CHUNK / 2;
    int CHUNK_SECTIONS = 16;
    int MAX_CHUNK_HEIGHT = CHUNK_DIMENSION * CHUNK_SECTIONS;

    World getWorld();

    int getChunkX();

    int getChunkZ();

    Block getBlockInChunk(int x, int y, int z);

    void setBlockInChunk(int x, int y, int z, Block block);

    byte getBlockLightInChunk(int x, int y, int z);

    void setBlockLightInChunk(int x, int y, int z, byte light);

    byte getSkyLightInChunk(int x, int y, int z);

    void setSkyLightInChunk(int x, int y, int z, byte light);

    void subscribe(Connection connection);

    void unsubscribe(Connection connection);

    void sendChunkData(Connection connection);
}
