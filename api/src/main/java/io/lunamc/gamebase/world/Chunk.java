package io.lunamc.gamebase.world;

import io.lunamc.common.network.Connection;
import io.lunamc.gamebase.block.Block;

public interface Chunk {

    int CHUNK_DIMENSION = 16;

    World getWorld();

    int getChunkX();

    int getChunkZ();

    Block getBlockInChunk(int x, int y, int z);

    void setBlockInChunk(int x, int y, int z, Block block);

    byte getSkyLightInChunk(int x, int y, int z);

    void setSkyLightInChunk(int x, int y, int z, byte light);

    void subscribe(Connection connection);

    void unsubscribe(Connection connection);

    void sendChunkData(Connection connection);
}
