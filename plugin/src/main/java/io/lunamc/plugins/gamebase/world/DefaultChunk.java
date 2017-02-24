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

import gnu.trove.map.TShortShortMap;
import gnu.trove.map.hash.TShortShortHashMap;
import io.lunamc.common.network.Connection;
import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.block.Block;
import io.lunamc.gamebase.world.Chunk;
import io.lunamc.gamebase.world.World;
import io.lunamc.plugins.gamebase.block.DefaultBlock;
import io.lunamc.plugins.gamebase.utils.BitLongWriter;
import io.lunamc.plugins.netty.network.NettyConnection;
import io.lunamc.protocol.ProtocolUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.StampedLock;

public class DefaultChunk implements Chunk {

    private static final String DEFAULT_MATERIAL = "minecraft:air";
    // 5 bytes for packet id, 8 byte for location, 5 bytes for block id
    private static final int PACKET_SIZE_BLOCK_CHANGE = 5 + 8 + 5;

    private final StampedLock lock = new StampedLock();
    private final TShortShortMap data = new TShortShortHashMap(20_000);
    private final ChunkSection[] chunkSections = new ChunkSection[CHUNK_SECTIONS];
    private final short[] blocksCounter = new short[CHUNK_SECTIONS];
    private final Set<NettyConnection> subscribers = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final HalfByteChunkStorage blockLight = new HalfByteChunkStorage();
    private final HalfByteChunkStorage skyLight;
    private final byte[] biomes = new byte[CHUNK_DIMENSION * CHUNK_DIMENSION];
    private final Game game;
    private final World world;
    private final int chunkX;
    private final int chunkZ;
    private int primaryBitMask = 0;

    public DefaultChunk(Game game, World world, int chunkX, int chunkZ) {
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.world = Objects.requireNonNull(world, "world must not be null");
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

        //blockLight.fill(0b1111);
        skyLight = world.getWorldType().supportsSkyLight() ? new HalfByteChunkStorage() : null;
        if (skyLight != null)
            skyLight.fill(0b1111);
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public int getChunkX() {
        return chunkX;
    }

    @Override
    public int getChunkZ() {
        return chunkZ;
    }

    @Override
    public Block getBlockInChunk(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("x must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);
        if (y < 0 || y >= MAX_CHUNK_HEIGHT)
            throw new IllegalArgumentException("y must be greater or equals 0 and smaller than " + MAX_CHUNK_HEIGHT);
        if (z < 0 || z >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("z must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);

        short key = createKey(x, y, z);
        long stamp = lock.readLock();
        try {
            if (!data.containsKey(key))
                return null;
            short index = data.get(key);
            ChunkSection chunkSection = chunkSections[y / CHUNK_DIMENSION];
            if (chunkSection != null)
                return chunkSection.getBlock(index);
        } finally {
            lock.unlockRead(stamp);
        }
        return null;
    }

    @Override
    public void setBlockInChunk(int x, int y, int z, Block block) {
        if (x < 0 || x >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("x must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);
        if (y < 0 || y >= MAX_CHUNK_HEIGHT)
            throw new IllegalArgumentException("y must be greater or equals 0 and smaller than " + MAX_CHUNK_HEIGHT);
        if (z < 0 || z >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("z must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);

        if (block != null && DEFAULT_MATERIAL.equals(block.getName()))
            block = null;

        short key = createKey(x, y, z);
        int value = block != null ? block.getPaletteId() : 0;

        boolean changed = false;
        long stamp = lock.readLock();
        try {
            if (value == 0 && !data.containsKey(key))
                return;

            int chunkSectionIndex = y / CHUNK_DIMENSION;
            while (true) {
                long writeStamp = lock.tryConvertToWriteLock(stamp);
                if (writeStamp != 0L) {
                    stamp = writeStamp;
                    if (value == 0) {
                        if (data.containsKey(key)) {
                            data.remove(key);
                            int count = --blocksCounter[chunkSectionIndex];
                            if (count <= 0)
                                removeChunkSection(chunkSectionIndex);
                            changed = true;
                        }
                    } else {
                        ChunkSection chunkSection = chunkSections[chunkSectionIndex];
                        if (chunkSection == null)
                            chunkSection = createChunkSection(chunkSectionIndex);

                        short index = chunkSection.getIndex(block);
                        short previous = data.put(key, index);
                        changed = previous != index;
                        if (changed)
                            blocksCounter[chunkSectionIndex]++;
                    }
                    break;
                } else {
                    lock.unlockRead(stamp);
                    stamp = lock.writeLock();
                }
            }
        } finally {
            lock.unlock(stamp);
        }

        if (changed)
            writeRelativeBlockChange(x, y, z, value);
    }

    @Override
    public byte getBlockLightInChunk(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("x must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);
        if (y < 0 || y >= MAX_CHUNK_HEIGHT)
            throw new IllegalArgumentException("y must be greater or equals 0 and smaller than " + MAX_CHUNK_HEIGHT);
        if (z < 0 || z >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("z must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);

        long stamp = lock.readLock();
        try {
            return blockLight.get(x, y, z);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public void setBlockLightInChunk(int x, int y, int z, byte light) {
        if (x < 0 || x >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("x must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);
        if (y < 0 || y >= MAX_CHUNK_HEIGHT)
            throw new IllegalArgumentException("y must be greater or equals 0 and smaller than " + MAX_CHUNK_HEIGHT);
        if (z < 0 || z >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("z must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);

        long stamp = lock.writeLock();
        try {
            blockLight.set(x, y, z, light);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public byte getSkyLightInChunk(int x, int y, int z) {
        if (skyLight == null)
            throw new IllegalStateException("Sky light not supported");
        if (x < 0 || x >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("x must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);
        if (y < 0 || y >= MAX_CHUNK_HEIGHT)
            throw new IllegalArgumentException("y must be greater or equals 0 and smaller than " + MAX_CHUNK_HEIGHT);
        if (z < 0 || z >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("z must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);

        long stamp = lock.readLock();
        try {
            return skyLight.get(x, y, z);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    @Override
    public void setSkyLightInChunk(int x, int y, int z, byte light) {
        if (skyLight == null)
            throw new IllegalStateException("Sky light not supported");
        if (x < 0 || x >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("x must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);
        if (y < 0 || y >= MAX_CHUNK_HEIGHT)
            throw new IllegalArgumentException("y must be greater or equals 0 and smaller than " + MAX_CHUNK_HEIGHT);
        if (z < 0 || z >= CHUNK_DIMENSION)
            throw new IllegalArgumentException("z must be greater or equals 0 and smaller than " + CHUNK_DIMENSION);

        long stamp = lock.writeLock();
        try {
            skyLight.set(x, y, z, light);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void subscribe(Connection connection) {
        if (!(connection instanceof NettyConnection))
            throw new IllegalArgumentException("connect must be an instance of " + NettyConnection.class.getName());
        subscribers.add((NettyConnection) connection);
    }

    @Override
    public void unsubscribe(Connection connection) {
        if (!(connection instanceof NettyConnection))
            throw new IllegalArgumentException("connect must be an instance of " + NettyConnection.class.getName());
        subscribers.remove(connection);
    }

    @Override
    public void sendChunkData(Connection connection) {
        if (!(connection instanceof NettyConnection))
            throw new IllegalArgumentException("connect must be an instance of " + NettyConnection.class.getName());
        Channel channel = ((NettyConnection) connection).channel();
        ByteBuf output = channel.alloc().buffer();
        // Write packet id for chunk data (0x20)
        ProtocolUtils.writeVarInt(output, 0x20);
        // Write chunk x
        output.writeInt(getChunkX());
        // Write chunk z
        output.writeInt(getChunkZ());
        // Write ground-up continuous
        // ToDo: Check when ground-up continuous should be false. Maybe for light updates?
        output.writeBoolean(true);

        ByteBuf chunkSectionData = channel.alloc().buffer();
        long stamp = lock.readLock();
        try {
            // Write primary bit mask
            ProtocolUtils.writeVarInt(output, primaryBitMask);

            for (int chunkSectionIndex = 0; chunkSectionIndex < chunkSections.length; chunkSectionIndex++) {
                int currentBitMask = 1 << chunkSectionIndex;
                if ((primaryBitMask & currentBitMask) != currentBitMask)
                    continue;

                ChunkSection chunkSection = chunkSections[chunkSectionIndex];
                int bitsPerBlock = chunkSection.getBitsPerBlock();

                // Write bits per block
                ProtocolUtils.writeVarInt(chunkSectionData, bitsPerBlock);

                // Palette
                if (bitsPerBlock >= 9) {
                    // Write palette length (palette is not used for >= 9 bits per block, so write 0)
                    ProtocolUtils.writeVarInt(chunkSectionData, 0);
                } else {
                    // Write palette
                    ProtocolUtils.writeVarIntArray(chunkSectionData, chunkSection.createPalette());
                }

                // Write chunk data
                // ToDo: Maybe use some sort of cached buffer?
                long[] chunkData = new long[BLOCKS_PER_CHUNK / (Long.SIZE / bitsPerBlock)];
                BitLongWriter writer = new BitLongWriter(chunkData, bitsPerBlock);
                short max = (short) (((chunkSectionIndex + 1) * CHUNK_DIMENSION) << 8);
                for (short i = (short) ((chunkSectionIndex * CHUNK_DIMENSION) << 8); i < max; i++) {
                    short reference = data.get(i);
                    if (bitsPerBlock >= 9)
                        writer.write(chunkSection.getBlock(reference).getPaletteId());
                    else
                        writer.write(reference);
                }
                // Write chunk data length in written longs
                ProtocolUtils.writeVarInt(chunkSectionData, chunkData.length);
                // Write chunk data
                for (long l : chunkData)
                    chunkSectionData.writeLong(l);

                // Write block light
                chunkSectionData.writeBytes(blockLight.array());

                if (skyLight != null) {
                    // Write sky light
                    chunkSectionData.writeBytes(skyLight.array());
                }
            }
            // Write biomes
            chunkSectionData.writeBytes(biomes);

            // Write length of chunk data
            ProtocolUtils.writeVarInt(output, chunkSectionData.readableBytes());
            // Write chunk data
            output.writeBytes(chunkSectionData);

            // Write length of block entities (comes later)
            ProtocolUtils.writeVarInt(output, 0);
        } finally {
            lock.unlockRead(stamp);
            chunkSectionData.release();
        }

        channel.writeAndFlush(output, channel.voidPromise());
    }

    @Override
    public String toString() {
        return getClass().getName() + "{chunkX=" + getChunkX() + ", chunkZ=" + getChunkZ() + '}';
    }

    protected ChunkSection createChunkSection(int chunkSectionIndex) {
        ChunkSection chunkSection = new ChunkSection();
        chunkSections[chunkSectionIndex] = chunkSection;
        primaryBitMask |= 1 << chunkSectionIndex;
        return chunkSection;
    }

    protected void removeChunkSection(int chunkSectionIndex) {
        chunkSections[chunkSectionIndex] = null;
        primaryBitMask &= ~(1 << chunkSectionIndex);
        blocksCounter[chunkSectionIndex] = 0;
    }

    private void writeRelativeBlockChange(int x, int y, int z, int value) {
        long position = (((x + (chunkX * CHUNK_DIMENSION)) & 0x3FFFFFFL) << 38) |
                ((y & 0xFFF) << 26) |
                ((z + (chunkZ * CHUNK_DIMENSION)) & 0x3FFFFFF);
        for (NettyConnection subscriber : subscribers) {
            Channel channel = subscriber.channel();
            ByteBuf output = channel.alloc().buffer(PACKET_SIZE_BLOCK_CHANGE);
            // Write packet id for block change (0x0b)
            ProtocolUtils.writeVarInt(output, 0x0b);
            // Write location
            output.writeLong(position);
            // Write block id
            ProtocolUtils.writeVarInt(output, value);
            // Goodbye bytes!
            channel.writeAndFlush(output, channel.voidPromise());
        }
    }

    private static short createKey(int x, int y, int z) {
        // bits: yyyyyyyyzzzzxxxx
        return (short) (((y & 0b11111111) << 8) | ((z & 0b1111) << 4) | (x & 0b1111));
    }

    private class ChunkSection {

        private final List<Block> palette = new ArrayList<>();
        private int bitsPerBlock = 4;

        private ChunkSection() {
            palette.add(DefaultBlock.AIR);
        }

        private int getBitsPerBlock() {
            return bitsPerBlock;
        }

        private Block getBlock(short index) {
            return palette.get(index);
        }

        private short getIndex(Block block) {
            int index = palette.indexOf(block);

            if (index < 0) {
                palette.add(block);
                index = palette.indexOf(block);
                if (palette.size() > (1 << bitsPerBlock)) {
                    bitsPerBlock++;
                    if (bitsPerBlock >= 9)
                        bitsPerBlock = Math.min(4, game.getBlockRegistry().getGlobalPaletteSize());
                }
            }

            return (short) index;
        }

        private int[] createPalette() {
            int[] result = new int[palette.size()];
            int i = 0;
            for (Block block : palette)
                result[i++] = block.getPaletteId();
            return result;
        }
    }
}
