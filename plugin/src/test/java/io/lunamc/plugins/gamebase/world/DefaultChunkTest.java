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

import io.lunamc.gamebase.block.Block;
import io.lunamc.gamebase.world.Chunk;
import io.lunamc.gamebase.world.World;
import io.lunamc.gamebase.world.WorldType;
import io.lunamc.plugins.netty.network.NettyConnection;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

public class DefaultChunkTest {

    private static final Block block1 = mockBlock("lunatest:demo1", 1);
    private static final Block block2 = mockBlock("lunatest:demo2", 2);
    private static final Block block3 = mockBlock("lunatest:demo3", 3);
    private static final Block blockAir = mockBlock("minecraft:air", 0);

    @Test
    public void testSetGetBlock() {
        Chunk chunk = createChunk();
        Assert.assertSame(block1, chunk.getBlockInChunk(4, 8, 15));
        Assert.assertSame(block2, chunk.getBlockInChunk(3, 3, 3));
        Assert.assertSame(block3, chunk.getBlockInChunk(7, 7, 7));
        Assert.assertSame(block3, chunk.getBlockInChunk(8, 8, 8));
        Assert.assertNull(chunk.getBlockInChunk(9, 9, 9));
        Assert.assertNull(chunk.getBlockInChunk(8, 3, 6));

        chunk.setBlockInChunk(4, 8, 15, blockAir);
        Assert.assertNull(chunk.getBlockInChunk(4, 8, 15));
    }

    @Test
    public void writeChunk() {
        ByteBuf buffer = Unpooled.buffer();
        NettyConnection connection = mockConnection(buffer);
        DefaultChunk chunk = createChunk();

        chunk.sendChunkData(connection);

        Assert.assertTrue(buffer.writerIndex() > 0);
    }

    private static Block mockBlock(String name, int paletteId) {
        Block block = Mockito.mock(Block.class);
        Mockito.when(block.getName()).thenReturn(name);
        Mockito.when(block.getPaletteId()).thenReturn(paletteId);
        return block;
    }

    private static NettyConnection mockConnection(ByteBuf buf) {
        Channel channel = Mockito.mock(Channel.class);
        Answer<ChannelFuture> answer = invocationOnMock -> {
            buf.writeBytes((ByteBuf) invocationOnMock.getArguments()[0]);
            return Mockito.mock(ChannelFuture.class);
        };
        Mockito.when(channel.write(Mockito.any())).then(answer);
        Mockito.when(channel.write(Mockito.any(), Mockito.any())).then(answer);
        Mockito.when(channel.writeAndFlush(Mockito.any())).then(answer);
        Mockito.when(channel.writeAndFlush(Mockito.any(), Mockito.any())).then(answer);
        Mockito.when(channel.alloc()).thenReturn(ByteBufAllocator.DEFAULT);

        NettyConnection connection = Mockito.mock(NettyConnection.class);
        Mockito.when(connection.channel()).thenReturn(channel);

        return connection;
    }

    private static DefaultChunk createChunk() {
        WorldType worldType = Mockito.mock(WorldType.class);
        Mockito.when(worldType.supportsSkyLight()).thenReturn(true);

        World world = Mockito.mock(World.class);
        Mockito.when(world.getWorldType()).thenReturn(worldType);

        DefaultChunk chunk = new DefaultChunk(world, 0, 0);
        chunk.setBlockInChunk(4, 8, 15, block1);
        chunk.setBlockInChunk(3, 3, 3, block2);
        chunk.setBlockInChunk(7, 7, 7, block3);
        chunk.setBlockInChunk(8, 8, 8, block3);
        chunk.setBlockInChunk(9, 9, 9, blockAir);

        return chunk;
    }
}
