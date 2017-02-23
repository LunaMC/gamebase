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

package io.lunamc.plugins.gamebase;

import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.block.Block;
import io.lunamc.gamebase.world.Chunk;
import io.lunamc.gamebase.world.World;
import io.lunamc.plugins.gamebase.world.DefaultWorld;
import io.lunamc.plugins.gamebase.world.StaticWorldType;
import io.lunamc.plugins.netty.network.NettyAuthorizedConnection;
import io.lunamc.protocol.ChannelHandlerContextUtils;
import io.lunamc.protocol.ProtocolUtils;
import io.lunamc.protocol.handler.PacketInboundHandlerAdapter;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class DefaultPlayHandler extends PacketInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultPlayHandler.class);

    private final World exampleWorld = new DefaultWorld(new StaticWorldType(true));
    private final Game game;
    private final NettyAuthorizedConnection connection;
    private boolean send;

    public DefaultPlayHandler(Game game, NettyAuthorizedConnection connection) {
        this.game = Objects.requireNonNull(game, "game must not be null");
        this.connection = Objects.requireNonNull(connection, "connection must not be null");

        Block block = game.getBlockRegistry().getBlockByName("minecraft:stone").orElseThrow(RuntimeException::new);
        for (int chunkX = -3; chunkX <= 3; chunkX++) {
            for (int chunkZ = -3; chunkZ <= 3; chunkZ++) {
                Chunk chunk = exampleWorld.requireChunk(chunkX, chunkZ);
                for (int x = 0; x < Chunk.CHUNK_DIMENSION; x++) {
                    for (int z = 0; z < Chunk.CHUNK_DIMENSION; z++)
                        chunk.setBlockInChunk(x, 64, z, block);
                }
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        writeJoinGame(ctx);
        writeBrand(ctx);
        writeSpawnPosition(ctx);
        writePlayerAbilities(ctx);
    }

    @Override
    protected void handlePacket(ChannelHandlerContext ctx, int packetId, ByteBuf content) throws Exception {
        switch (packetId) {
            case 0x00:
                // Teleport confirm
                handleTeleportConfirm(ctx, content);
                break;
            case 0x04:
                // Client settings
                handleClientSettings(ctx, content);
                break;
            case 0x0D:
                // Player position and look
                handlePlayerPositionAndLook();
                break;
        }
    }

    private void handleClientSettings(ChannelHandlerContext ctx, ByteBuf content) {
        String locale = ProtocolUtils.readString(content);
        byte viewDistance = content.readByte();
        int chatMode = ProtocolUtils.readVarInt(content);
        boolean chatColors = content.readBoolean();
        byte displaySkinParts = content.readByte();
        int mainHand = ProtocolUtils.readVarInt(content);

        LOGGER.info("Got client settings from {}: {} {} {} {} {} {}", ChannelHandlerContextUtils.client(ctx), locale, viewDistance, chatMode, chatColors, displaySkinParts, mainHand);

        writePlayPositionAndLook(ctx);
    }

    private void handlePlayerPositionAndLook() {
        if (!send) {
            send = true;
            for (int chunkX = -3; chunkX <= 3; chunkX++) {
                for (int chunkZ = -3; chunkZ <= 3; chunkZ++) {
                    exampleWorld.requireChunk(chunkX, chunkZ).sendChunkData(connection);
                }
            }
        }
    }

    private static void writeJoinGame(ChannelHandlerContext ctx) {
        ByteBuf out = ctx.alloc().buffer();
        // Write packet id of join game (0x23)
        ProtocolUtils.writeVarInt(out, 0x23);
        // Write entity id
        out.writeInt(42);
        // Write game mode (0 = survival)
        out.writeByte(0);
        // Write dimension (0 = overworld)
        out.writeInt(0);
        // Write difficulty (0 = peaceful)
        out.writeByte(0);
        // Write max players
        out.writeByte(1);
        // Write level type
        ProtocolUtils.writeString(out, "default");
        // Write reduced debug info
        out.writeBoolean(false);

        // Send it to the client
        ctx.writeAndFlush(out, ctx.voidPromise());
    }

    private static void writeBrand(ChannelHandlerContext ctx) {
        ByteBuf data = ctx.alloc().buffer();
        ByteBuf out = ctx.alloc().buffer();
        try {
            // Write plugin message data
            ProtocolUtils.writeString(data, "LunaMC_Game");

            // Write packet id of plugin message (0x18)
            ProtocolUtils.writeVarInt(out, 0x18);
            // Write channel name
            ProtocolUtils.writeString(out, "MC|Brand");
            // Write plugin message
            ProtocolUtils.writeVarInt(out, data.readableBytes());
            out.writeBytes(data);
        } finally {
            data.release();
        }

        // Send it to the client
        ctx.writeAndFlush(out, ctx.voidPromise());
    }

    private static void writeSpawnPosition(ChannelHandlerContext ctx) {
        ByteBuf out = ctx.alloc().buffer();
        // Write packet id for spawn position (0x43)
        ProtocolUtils.writeVarInt(out, 0x43);
        // Write Location
        out.writeLong(((8L & 0x3FFFFFF) << 38) | ((68L & 0xFFF) << 26) | (8L & 0x3FFFFFF));
        ctx.writeAndFlush(out);
    }

    private static void writePlayerAbilities(ChannelHandlerContext ctx) {
        ByteBuf out = ctx.alloc().buffer();
        // Write packet id for player ability (0x2b)
        ProtocolUtils.writeVarInt(out, 0x2b);
        // Write flags (0x01 = invulnerable, 0x02 = flying, 0x04 = allow flying)
        //out.writeByte(0x01 & 0x02 & 0x04);
        out.writeByte(0x00);
        // Write flying speed
        out.writeFloat(1);
        // Write field of view modifier
        out.writeFloat(1);
        ctx.writeAndFlush(out);
    }

    private static void writePlayPositionAndLook(ChannelHandlerContext ctx) {
        ByteBuf out = ctx.alloc().buffer();
        // Write packet id for player position and look (0x2e)
        ProtocolUtils.writeVarInt(out, 0x2e);
        // Write x
        out.writeDouble(8);
        // Write y
        out.writeDouble(68);
        // Write z
        out.writeDouble(8);
        // Write yaw
        out.writeFloat(0);
        // Write pitch
        out.writeFloat(0);
        // Write flags (no flags set)
        out.writeByte(0x00);
        // Write teleport id
        ProtocolUtils.writeVarInt(out, 1);
        ctx.writeAndFlush(out, ctx.voidPromise());
    }

    private static void handleTeleportConfirm(ChannelHandlerContext ctx, ByteBuf content) {
        int teleportId = ProtocolUtils.readVarInt(content);
        LOGGER.info("Client {} confirms teleport with teleportId {}", ChannelHandlerContextUtils.client(ctx), teleportId);
    }
}
