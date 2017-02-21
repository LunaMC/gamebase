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

package io.lunamc.plugins.gamebase.world;

import gnu.trove.map.TLongObjectMap;
import gnu.trove.map.hash.TLongObjectHashMap;
import io.lunamc.gamebase.world.Chunk;
import io.lunamc.gamebase.world.World;
import io.lunamc.gamebase.world.WorldType;

import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

public class DefaultWorld implements World {

    private final StampedLock chunksLock = new StampedLock();
    private final TLongObjectMap<Chunk> chunks = new TLongObjectHashMap<>();
    private final WorldType worldType;

    public DefaultWorld(WorldType worldType) {
        this.worldType = Objects.requireNonNull(worldType, "worldType must not be null");
    }

    @Override
    public WorldType getWorldType() {
        return worldType;
    }

    @Override
    public Chunk requireChunk(int chunkX, int chunkZ) {
        long stamp = chunksLock.readLock();
        try {
            long key = createChunkKey(chunkX, chunkZ);
            Chunk chunk = chunks.get(key);
            if (chunk == null) {
                while (true) {
                    long writeStamp = chunksLock.tryConvertToWriteLock(stamp);
                    if (writeStamp != 0L) {
                        stamp = writeStamp;
                        chunk = chunks.get(key);
                        if (chunk == null) {
                            chunk = new DefaultChunk(this, chunkX, chunkZ);
                            chunks.put(key, chunk);
                        }
                        break;
                    } else {
                        chunksLock.unlockRead(stamp);
                        stamp = chunksLock.writeLock();
                    }
                }
            }
            return chunk;
        } finally {
            chunksLock.unlock(stamp);
        }
    }

    private static long createChunkKey(int chunkX, int chunkZ) {
        return (((long) chunkX) << Integer.SIZE) | chunkZ;
    }
}
