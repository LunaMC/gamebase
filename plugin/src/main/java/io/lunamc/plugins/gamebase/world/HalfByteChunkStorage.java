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

import io.lunamc.gamebase.world.Chunk;

public class HalfByteChunkStorage {

    private final byte[] storage = new byte[Chunk.HALF_BLOCKS_PER_CHUNK];

    public byte get(int x, int y, int z) {
        int index = createKey(x, y, z);
        return (byte) (index % 2 == 0 ? storage[index] & 0b00001111 : (storage[index] >> 4) & 0b1111);
    }

    public void set(int x, int y, int z, byte value) {
        if (value < 0b0000 || value > 0b1111)
            throw new IllegalArgumentException("value must be between 0 and 16");

        int index = createKey(x, y, z);
        if (index % 2 == 0)
            storage[index] = (byte) ((storage[index] & 0b11110000) | (value & 0b00001111));
        else
            storage[index] = (byte) ((storage[index] & 0b00001111) | (value << 4));
    }

    public byte[] array() {
        return storage;
    }

    public void fill(int value) {
        value = (value << 4) | value;
        for (int i = 0; i < storage.length; i++)
            storage[i] = (byte) value;
    }

    private static int createKey(int x, int y, int z) {
        return (((y & 0b11111111) << 8) | ((z & 0b1111) << 4) | (x & 0b1111)) / 2;
    }
}
