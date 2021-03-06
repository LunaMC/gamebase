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

import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.world.Chunk;
import io.lunamc.gamebase.world.World;
import io.lunamc.gamebase.world.WorldType;
import org.eclipse.collections.api.map.primitive.MutableLongObjectMap;
import org.eclipse.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.util.Objects;
import java.util.concurrent.locks.StampedLock;

public class DefaultWorld implements World {

    private final StampedLock chunksLock = new StampedLock();
    private final MutableLongObjectMap<Chunk> chunks = new LongObjectHashMap<>();
    private final Game game;
    private final WorldType worldType;

    public DefaultWorld(Game game, WorldType worldType) {
        this.game = Objects.requireNonNull(game, "game must not be null");
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
                            chunk = new DefaultChunk(game, this, chunkX, chunkZ);
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
