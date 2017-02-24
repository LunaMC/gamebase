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

import io.lunamc.common.host.VirtualHost;
import io.lunamc.common.status.StatusProvider;
import io.lunamc.gamebase.Game;
import io.lunamc.gamebase.WorldManager;
import io.lunamc.gamebase.block.Block;
import io.lunamc.gamebase.entity.EntityIdAllocator;
import io.lunamc.gamebase.entity.Player;
import io.lunamc.gamebase.math.vector.VectorFactory;
import io.lunamc.gamebase.world.Chunk;
import io.lunamc.gamebase.world.World;
import io.lunamc.platform.service.ServiceRegistration;
import io.lunamc.plugins.gamebase.block.DefaultBlockRegistry;
import io.lunamc.plugins.gamebase.entity.DefaultEntityIdAllocator;
import io.lunamc.plugins.gamebase.utils.LazyHolder;
import io.lunamc.plugins.gamebase.world.DefaultFuzzyLocation;
import io.lunamc.plugins.gamebase.world.DefaultWorld;
import io.lunamc.plugins.gamebase.world.StaticWorldType;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultGame implements Game {

    private final DefaultEntityIdAllocator entityIdAllocator = new DefaultEntityIdAllocator();
    private final DefaultBlockRegistry blockRegistry = new DefaultBlockRegistry();
    private final DefaultWorldManager worldManager;
    private final Set<Player> connections = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<VirtualHost> handledVirtualHosts = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicReference<StatusProvider> statusProvider = new AtomicReference<>();

    public DefaultGame(ServiceRegistration<VectorFactory> vectorFactory) {


        worldManager = new DefaultWorldManager(LazyHolder.create(
                () -> {
                    World world = new DefaultWorld(this, new StaticWorldType(true));
                    Block block = getBlockRegistry().getBlockByName("minecraft:stone").orElseThrow(RuntimeException::new);
                    for (int chunkX = -3; chunkX <= 3; chunkX++) {
                        for (int chunkZ = -3; chunkZ <= 3; chunkZ++) {
                            Chunk chunk = world.requireChunk(chunkX, chunkZ);
                            for (int x = 0; x < Chunk.CHUNK_DIMENSION; x++) {
                                for (int z = 0; z < Chunk.CHUNK_DIMENSION; z++)
                                    chunk.setBlockInChunk(x, 64, z, block);
                            }
                        }
                    }

                    return new DefaultFuzzyLocation(
                            world,
                            vectorFactory.requireInstance().createVector3f(0, 68, 0),
                            0,
                            0
                    );
                }
        ));
    }

    @Override
    public EntityIdAllocator getEntityIdAllocator() {
        return entityIdAllocator;
    }

    @Override
    public DefaultBlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    @Override
    public WorldManager getWorldManager() {
        return worldManager;
    }

    @Override
    public Collection<Player> getPlayers() {
        return connections;
    }

    @Override
    public Collection<VirtualHost> getHandledVirtualHosts() {
        return handledVirtualHosts;
    }

    @Override
    public StatusProvider getStatusProvider() {
        return statusProvider.get();
    }

    @Override
    public void setStatusProvider(StatusProvider statusProvider) {
        this.statusProvider.set(statusProvider);
    }
}
