/*
 *  Copyright 2017 LunaMC.io
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
