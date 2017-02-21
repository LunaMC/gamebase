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

import java.util.Objects;

public class DefaultBlock implements Block {

    public static final DefaultBlock AIR = new DefaultBlock("minecraft:air", 0x00);

    private final String name;
    private final int paletteId;

    public DefaultBlock(String name, int paletteId) {
        this.name = name;
        this.paletteId = paletteId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPaletteId() {
        return paletteId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Block))
            return false;
        Block block = (Block) o;
        return getPaletteId() == block.getPaletteId() &&
                Objects.equals(getName(), block.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getPaletteId());
    }

    @Override
    public String toString() {
        return getClass().getName() + "{name=\"" + getName() + "\", paletteId=" + getPaletteId() + '}';
    }
}
