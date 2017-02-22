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

package io.lunamc.plugins.gamebase.block;

import com.google.common.math.IntMath;
import io.lunamc.gamebase.block.Block;
import io.lunamc.gamebase.block.BlockRegistry;

import java.math.RoundingMode;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.StampedLock;

public class DefaultBlockRegistry implements BlockRegistry {

    private static final int MAX_VARIANTS = 0b1111;

    private final ConcurrentMap<String, BlockRegistration> blocks = new ConcurrentHashMap<>();
    private int globalPaletteSize;

    public DefaultBlockRegistry() {
        putBlock(DefaultBlock.AIR);
    }

    @Override
    public Optional<Block> getBlockByName(String name, int variant) {
        if (variant > MAX_VARIANTS)
            throw new IllegalArgumentException("variant must be between 0 and " + MAX_VARIANTS);
        BlockRegistration registration = blocks.get(name);
        if (registration == null)
            return Optional.empty();
        return Optional.ofNullable(registration.getVariant(variant));
    }

    @Override
    public Block register(String name, int paletteId) {
        return putBlock(new DefaultBlock(name, paletteId));
    }

    @Override
    public int getGlobalPaletteSize() {
        if (globalPaletteSize < 0)
            globalPaletteSize = IntMath.log2(blocks.size() * MAX_VARIANTS, RoundingMode.UP);
        return globalPaletteSize;
    }

    private Block putBlock(DefaultBlock block)  {
        String name = block.getName();
        BlockRegistration registration = blocks.computeIfAbsent(name, key -> new BlockRegistration());
        int variant = block.getPaletteId() & 0b1111;
        boolean inserted = registration.setVariant(variant, block);
        if (!inserted)
            throw new UnsupportedOperationException("A block identified by \"" + name + "\" is already registered with a different specification");
        else
            globalPaletteSize = -1;
        return block;
    }

    private static class BlockRegistration {

        private final DefaultBlock[] variants = new DefaultBlock[MAX_VARIANTS];
        private final StampedLock lock = new StampedLock();

        public DefaultBlock getVariant(int variant) {
            long stamp = lock.readLock();
            try {
                return variants[variant];
            } finally {
                lock.unlockRead(stamp);
            }
        }

        public boolean setVariant(int variant, DefaultBlock block) {
            long stamp = lock.readLock();
            try {
                if (variants[variant] != null)
                    return false;
                while(true) {
                    long writeStamp = lock.tryConvertToWriteLock(stamp);
                    if (writeStamp != 0) {
                        stamp = writeStamp;
                        if (variants[variant] != null)
                            return false;
                        variants[variant] = block;
                        return true;
                    } else {
                        lock.unlockRead(stamp);
                        stamp = lock.writeLock();
                    }
                }
            } finally {
                lock.unlock(stamp);
            }
        }
    }
}
