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
