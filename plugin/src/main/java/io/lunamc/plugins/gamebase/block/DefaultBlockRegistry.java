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
